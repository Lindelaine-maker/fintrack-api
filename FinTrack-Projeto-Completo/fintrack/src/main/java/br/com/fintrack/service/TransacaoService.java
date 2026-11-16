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
    private final TransacaoRepository transacoes; private final CategoriaRepository categorias;
    private final UsuarioService usuarioService; private final ModelMapper mapper;
    public TransacaoService(TransacaoRepository transacoes,CategoriaRepository categorias,UsuarioService usuarioService,ModelMapper mapper){
        this.transacoes=transacoes;this.categorias=categorias;this.usuarioService=usuarioService;this.mapper=mapper;
    }

    @Transactional(readOnly=true)
    public List<TransacaoDTO> listar(Authentication auth,LocalDate inicio,LocalDate fim,Long categoriaId){
        if(inicio!=null && fim!=null && inicio.isAfter(fim))
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        Long uid=usuarioService.usuarioAtual(auth).getId();
        return transacoes.buscarComFiltros(uid,inicio,fim,categoriaId).stream().map(this::dto).toList();
    }

    @Transactional
    public TransacaoDTO criar(TransacaoDTO dto,Authentication auth){
        Usuario u=usuarioService.usuarioAtual(auth);Categoria c=categoria(dto.categoriaId(),u.getId());
        validarCategoriaTipo(dto.tipo(),c);validarSaldo(dto.valor(),dto.tipo(),u.getId(),null);
        Transacao t=new Transacao();preencher(t,dto,c,u);return dto(transacoes.save(t));
    }

    @Transactional
    public TransacaoDTO atualizar(Long id,TransacaoDTO dto,Authentication auth){
        Usuario u=usuarioService.usuarioAtual(auth);
        Transacao t=transacoes.findByIdAndUsuarioId(id,u.getId()).orElseThrow(()->new ResourceNotFoundException("Transação não encontrada."));
        Categoria c=categoria(dto.categoriaId(),u.getId());validarCategoriaTipo(dto.tipo(),c);
        validarSaldo(dto.valor(),dto.tipo(),u.getId(),id);
        preencher(t,dto,c,u);return dto(t);
    }

    @Transactional
    public void excluir(Long id,Authentication auth){
        Long uid=usuarioService.usuarioAtual(auth).getId();
        Transacao t=transacoes.findByIdAndUsuarioId(id,uid).orElseThrow(()->new ResourceNotFoundException("Transação não encontrada."));
        transacoes.delete(t);
    }

    @Transactional(readOnly=true)
    public SaldoDTO saldo(Authentication auth){
        Long uid=usuarioService.usuarioAtual(auth).getId();
        BigDecimal receitas=transacoes.sumValorByUsuarioIdAndTipo(uid,TipoTransacao.RECEITA);
        BigDecimal despesas=transacoes.sumValorByUsuarioIdAndTipo(uid,TipoTransacao.DESPESA);
        return new SaldoDTO(receitas,despesas,receitas.subtract(despesas));
    }

    @Transactional(readOnly=true)
    public boolean isOwner(Authentication auth,Long id){
        return auth!=null && transacoes.existsByIdAndUsuarioId(id,usuarioService.usuarioAtual(auth).getId());
    }

    private Categoria categoria(Long id,Long uid){
        return categorias.findByIdAndUsuarioId(id,uid).orElseThrow(()->new ResourceNotFoundException("Categoria não encontrada para este usuário."));
    }
    private void validarCategoriaTipo(TipoTransacao tipo,Categoria c){
        if(c.getTipo()!=tipo) throw new IllegalArgumentException("O tipo da transação deve ser igual ao tipo da categoria.");
    }
    private void validarSaldo(BigDecimal valor,TipoTransacao tipo,Long uid,Long id){
        if(tipo!=TipoTransacao.DESPESA)return;
        BigDecimal receitas=id==null?transacoes.sumValorByUsuarioIdAndTipo(uid,TipoTransacao.RECEITA):transacoes.sumValorByUsuarioIdAndTipoExcludingId(uid,TipoTransacao.RECEITA,id);
        BigDecimal despesas=id==null?transacoes.sumValorByUsuarioIdAndTipo(uid,TipoTransacao.DESPESA):transacoes.sumValorByUsuarioIdAndTipoExcludingId(uid,TipoTransacao.DESPESA,id);
        if(despesas.add(valor).compareTo(receitas)>0) throw new SaldoInsuficienteException("A despesa ultrapassa o saldo disponível.");
    }
    private void preencher(Transacao t,TransacaoDTO d,Categoria c,Usuario u){
        t.setDescricao(d.descricao().trim());t.setValor(d.valor());t.setData(d.data());t.setTipo(d.tipo());t.setCategoria(c);t.setUsuario(u);
    }
    private TransacaoDTO dto(Transacao t){
        return new TransacaoDTO(t.getId(),t.getDescricao(),t.getValor(),t.getData(),t.getTipo(),t.getCategoria().getId(),t.getCategoria().getNome());
    }
}
