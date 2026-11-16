package br.com.fintrack.service;

import br.com.fintrack.dto.*;
import br.com.fintrack.exception.*;
import br.com.fintrack.model.*;
import br.com.fintrack.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository transacoes;
    private final CategoriaRepository categorias;
    private final UsuarioService usuarioService;
    private final ModelMapper mapper;

    public TransacaoService(TransacaoRepository transacoes, CategoriaRepository categorias,
                            UsuarioService usuarioService, ModelMapper mapper) {
        this.transacoes = transacoes;
        this.categorias = categorias;
        this.usuarioService = usuarioService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> listar(Authentication auth, LocalDate inicio, LocalDate fim, Long categoriaId) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        Long uid = usuarioService.usuarioAtual(auth).getId();
        return transacoes.buscarComFiltros(uid, inicio, fim, categoriaId).stream()
                .map(this::dto).toList();
    }

    @Transactional
    public TransacaoDTO criar(TransacaoDTO dto, Authentication auth) {
        // Bloqueio por usuário serializa validação de saldo, criação e mudanças de categoria.
        Usuario usuario = usuarioService.usuarioAtualComBloqueio(auth);
        Categoria categoria = categoria(dto.categoriaId(), usuario.getId());
        validarCategoriaTipo(dto.tipo(), categoria);
        validarSaldo(dto.valor(), dto.tipo(), usuario.getId(), null);

        Transacao transacao = mapper.map(dto, Transacao.class);
        // O ID é gerado pelo servidor; nunca aceitar ID enviado no corpo do POST.
        transacao.setId(null);
        transacao.setDescricao(dto.descricao().trim());
        associarProprietarioECategoria(transacao, categoria, usuario);
        return dto(transacoes.save(transacao));
    }

    @Transactional
    public TransacaoDTO atualizar(Long id, TransacaoDTO dto, Authentication auth) {
        Usuario usuario = usuarioService.usuarioAtualComBloqueio(auth);
        Transacao transacao = transacoes.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));
        Categoria categoria = categoria(dto.categoriaId(), usuario.getId());
        validarCategoriaTipo(dto.tipo(), categoria);
        validarSaldo(dto.valor(), dto.tipo(), usuario.getId(), id);

        Transacao dadosAtualizados = mapper.map(dto, Transacao.class);
        transacao.setDescricao(dadosAtualizados.getDescricao().trim());
        transacao.setValor(dadosAtualizados.getValor());
        transacao.setData(dadosAtualizados.getData());
        transacao.setTipo(dadosAtualizados.getTipo());
        associarProprietarioECategoria(transacao, categoria, usuario);
        return dto(transacao);
    }

    @Transactional
    public void excluir(Long id, Authentication auth) {
        Usuario usuario = usuarioService.usuarioAtualComBloqueio(auth);
        Transacao transacao = transacoes.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));
        transacoes.delete(transacao);
    }

    @Transactional(readOnly = true)
    public SaldoDTO saldo(Authentication auth) {
        Long uid = usuarioService.usuarioAtual(auth).getId();
        Object[] totais = transacoes.calcularTotaisPorUsuario(uid, TipoTransacao.RECEITA, TipoTransacao.DESPESA);
        BigDecimal receitas = totais[0] == null ? BigDecimal.ZERO : (BigDecimal) totais[0];
        BigDecimal despesas = totais[1] == null ? BigDecimal.ZERO : (BigDecimal) totais[1];
        return new SaldoDTO(receitas, despesas, receitas.subtract(despesas));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication auth, Long id) {
        if (auth == null) return false;
        return transacoes.pertenceAoUsuario(id, usuarioService.usuarioAtual(auth).getId())
                .orElse(true);
    }

    private Categoria categoria(Long id, Long uid) {
        return categorias.findByIdAndUsuarioId(id, uid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada para este usuário."));
    }

    private void validarCategoriaTipo(TipoTransacao tipo, Categoria categoria) {
        if (categoria.getTipo() != tipo)
            throw new IllegalArgumentException("O tipo da transação deve ser igual ao tipo da categoria.");
    }

    private void validarSaldo(BigDecimal valor, TipoTransacao tipo, Long uid, Long id) {
        if (tipo != TipoTransacao.DESPESA) return;
        BigDecimal receitas = id == null
                ? transacoes.sumValorByUsuarioIdAndTipo(uid, TipoTransacao.RECEITA)
                : transacoes.sumValorByUsuarioIdAndTipoExcludingId(uid, TipoTransacao.RECEITA, id);
        BigDecimal despesas = id == null
                ? transacoes.sumValorByUsuarioIdAndTipo(uid, TipoTransacao.DESPESA)
                : transacoes.sumValorByUsuarioIdAndTipoExcludingId(uid, TipoTransacao.DESPESA, id);
        if (despesas.add(valor).compareTo(receitas) > 0)
            throw new SaldoInsuficienteException("A despesa ultrapassa o saldo disponível.");
    }

    private void associarProprietarioECategoria(Transacao transacao, Categoria categoria, Usuario usuario) {
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);
    }

    private TransacaoDTO dto(Transacao transacao) {
        // ModelMapper é usado no mapeamento de entrada (record -> entidade).
        // A saída é construída explicitamente porque records são imutáveis e o ModelMapper
        // tenta instanciá-los por reflexão, o que falha para este tipo de destino.
        return new TransacaoDTO(transacao.getId(), transacao.getDescricao(), transacao.getValor(),
                transacao.getData(), transacao.getTipo(), transacao.getCategoria().getId(),
                transacao.getCategoria().getNome());
    }
}