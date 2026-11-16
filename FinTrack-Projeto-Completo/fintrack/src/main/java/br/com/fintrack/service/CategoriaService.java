package br.com.fintrack.service;
import br.com.fintrack.dto.CategoriaDTO;
import br.com.fintrack.exception.ResourceConflictException;
import br.com.fintrack.exception.ResourceNotFoundException;
import br.com.fintrack.model.Categoria;
import br.com.fintrack.repository.CategoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repo; private final UsuarioService usuarioService;
    public CategoriaService(CategoriaRepository repo,UsuarioService usuarioService){this.repo=repo;this.usuarioService=usuarioService;}

    @Transactional(readOnly=true)
    public List<CategoriaDTO> listar(Authentication auth){
        return repo.findByUsuarioIdOrderByNomeAsc(usuarioService.usuarioAtual(auth).getId()).stream().map(this::dto).toList();
    }

    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto,Authentication auth){
        var u=usuarioService.usuarioAtual(auth);
        String nome=dto.nome().trim();
        if(repo.existsByNomeIgnoreCaseAndUsuarioId(nome,u.getId()))
            throw new ResourceConflictException("Já existe uma categoria com esse nome.");
        Categoria c=new Categoria();c.setNome(nome);c.setDescricao(dto.descricao());c.setTipo(dto.tipo());c.setUsuario(u);
        return dto(repo.save(c));
    }

    @Transactional
    public CategoriaDTO atualizar(Long id,CategoriaDTO dto,Authentication auth){
        var u=usuarioService.usuarioAtual(auth);
        var c=repo.findByIdAndUsuarioId(id,u.getId()).orElseThrow(()->new ResourceNotFoundException("Categoria não encontrada."));
        String nome=dto.nome().trim();
        if(repo.existsByNomeIgnoreCaseAndUsuarioIdAndIdNot(nome,u.getId(),id))
            throw new ResourceConflictException("Já existe outra categoria com esse nome.");
        c.setNome(nome);c.setDescricao(dto.descricao());c.setTipo(dto.tipo());return dto(c);
    }

    @Transactional
    public void excluir(Long id,Authentication auth){
        var u=usuarioService.usuarioAtual(auth);
        var c=repo.findByIdAndUsuarioId(id,u.getId()).orElseThrow(()->new ResourceNotFoundException("Categoria não encontrada."));
        if(!c.getTransacoes().isEmpty())
            throw new ResourceConflictException("Não é possível excluir uma categoria que possui transações vinculadas.");
        repo.delete(c);
    }

    @Transactional(readOnly=true)
    public boolean isOwner(Authentication auth,Long id){
        return auth!=null && repo.existsByIdAndUsuarioId(id,usuarioService.usuarioAtual(auth).getId());
    }

    private CategoriaDTO dto(Categoria c){return new CategoriaDTO(c.getId(),c.getNome(),c.getDescricao(),c.getTipo());}
}
