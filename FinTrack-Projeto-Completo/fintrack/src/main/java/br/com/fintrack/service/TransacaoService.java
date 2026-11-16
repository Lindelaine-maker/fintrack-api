package br.com.fintrack.service;

import br.com.fintrack.dto.*;
import br.com.fintrack.exception.*;
import br.com.fintrack.model.*;
import br.com.fintrack.repository.*;
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

    public TransacaoService(TransacaoRepository transacoes, CategoriaRepository categorias,
                            UsuarioService usuarioService) {
        this.transacoes = transacoes;
        this.categorias = categorias;
        this.usuarioService = usuarioService;
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

        Transacao transacao = novaTransacao(dto);
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

        transacao.setDescricao(dto.descricao().trim());
        transacao.setValor(dto.valor());
        transacao.setData(dto.data());
        transacao.setTipo(dto.tipo());
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
        // Consultas tipadas evitam casts frágeis do resultado agregado (Object[]) entre bancos.
        BigDecimal receitas = transacoes.sumValorByUsuarioIdAndTipo(uid, TipoTransacao.RECEITA);
        BigDecimal despesas = transacoes.sumValorByUsuarioIdAndTipo(uid, TipoTransacao.DESPESA);
        receitas = receitas == null ? BigDecimal.ZERO : receitas;
        despesas = despesas == null ? BigDecimal.ZERO : despesas;
        return new SaldoDTO(receitas, despesas, receitas.subtract(despesas));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication auth, Long id) {
        if (auth == null) return false;
        return transacoes.pertenceAoUsuario(id, usuarioService.usuarioAtual(auth).getId())
                .orElse(true);
    }

    private Transacao novaTransacao(TransacaoDTO dto) {
        Transacao transacao = new Transacao();
        transacao.setDescricao(dto.descricao().trim());
        transacao.setValor(dto.valor());
        transacao.setData(dto.data());
        transacao.setTipo(dto.tipo());
        return transacao;
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
        // O DTO de saída é um record imutável, construído explicitamente.
        return new TransacaoDTO(transacao.getId(), transacao.getDescricao(), transacao.getValor(),
                transacao.getData(), transacao.getTipo(), transacao.getCategoria().getId(),
                transacao.getCategoria().getNome());
    }
}