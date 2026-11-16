package br.com.fintrack.service;
import br.com.fintrack.dto.CategoriaDTO;
import br.com.fintrack.exception.ResourceNotFoundException;
import br.com.fintrack.model.Categoria;
import br.com.fintrack.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repo; private final UsuarioService usuarioService;
    public CategoriaService(CategoriaRepository repo,UsuarioService usuarioService){this.repo=repo;this.usuarioService=usuarioService;}
    @Transactional(readOnly=true)
    public List<CategoriaDTO> listar(org.springframework.security.core.Authentication auth){
        return repo.findByUsuarioIdOrderByNomeAsc(usuarioService.usuarioAtual(auth).getId()).stream().map(this::dto).toList();
    }
    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto,org.springframework.security.core.Authentication auth){
        var u=usuarioService.usuarioAtual(auth);
        if(repo.existsByNomeIgnoreCaseAndUsuarioId(dto.nome().trim(),u.getId())) throw new IllegalArgumentException("Já existe uma categoria com esse nome.");
        Categoria c=new Categoria();c.setNome(dto.nome().trim());c.setDescricao(dto.descricao());c.setTipo(dto.tipo());c.setUsuario(u);
        return dto(repo.save(c));
    }
    @Transactional
    public CategoriaDTO atualizar(Long id,CategoriaDTO dto,org.springframework.security.core.Authentication auth){
        var u=usuarioService.usuarioAtual(auth);var c=repo.findByIdAndUsuarioId(id,u.getId()).orElseThrow(()->new ResourceNotFoundException("Categoria não encontrada."));
        c.setNome(dto.nome().trim());c.setDescricao(dto.descricao());c.setTipo(dto.tipo());return dto(c);
    }
    @Transactional
    public void excluir(Long id,org.springframework.security.core.Authentication auth){
        var u=usuarioService.usuarioAtual(auth);var c=repo.findByIdAndUsuarioId(id,u.getId()).orElseThrow(()->new ResourceNotFoundException("Categoria não encontrada."));
        repo.delete(c);
    }
    private CategoriaDTO dto(Categoria c){return new CategoriaDTO(c.getId(),c.getNome(),c.getDescricao(),c.getTipo());}
}
