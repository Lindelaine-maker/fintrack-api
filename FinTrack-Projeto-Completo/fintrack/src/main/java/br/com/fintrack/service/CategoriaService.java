package br.com.fintrack.service;
import br.com.fintrack.dto.CategoriaDTO;
import br.com.fintrack.exception.ResourceConflictException;
import br.com.fintrack.exception.ResourceNotFoundException;
import br.com.fintrack.model.Categoria;
import br.com.fintrack.repository.CategoriaRepository;
import br.com.fintrack.repository.TransacaoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repo;
    private final TransacaoRepository transacoes;
    private final UsuarioService usuarioService;

    public CategoriaService(CategoriaRepository repo, TransacaoRepository transacoes, UsuarioService usuarioService) {
        this.repo = repo;
        this.transacoes = transacoes;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar(Authentication auth) {
        return repo.findByUsuarioIdOrderByNomeAsc(usuarioService.usuarioAtual(auth).getId())
                .stream().map(this::dto).toList();
    }

    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto, Authentication auth) {
        var usuario = usuarioService.usuarioAtual(auth);
        String nome = dto.nome().trim();
        if (repo.existsByNomeIgnoreCaseAndUsuarioId(nome, usuario.getId()))
            throw new ResourceConflictException("Já existe uma categoria com esse nome.");
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(dto.descricao());
        categoria.setTipo(dto.tipo());
        categoria.setUsuario(usuario);
        return dto(repo.save(categoria));
    }

    @Transactional
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto, Authentication auth) {
        var usuario = usuarioService.usuarioAtual(auth);
        var categoria = repo.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        String nome = dto.nome().trim();
        if (repo.existsByNomeIgnoreCaseAndUsuarioIdAndIdNot(nome, usuario.getId(), id))
            throw new ResourceConflictException("Já existe outra categoria com esse nome.");
        if (categoria.getTipo() != dto.tipo() && transacoes.existsByCategoriaId(id))
            throw new ResourceConflictException("Não é possível alterar o tipo de uma categoria que possui transações.");
        categoria.setNome(nome);
        categoria.setDescricao(dto.descricao());
        categoria.setTipo(dto.tipo());
        return dto(categoria);
    }

    @Transactional
    public void excluir(Long id, Authentication auth) {
        var usuario = usuarioService.usuarioAtual(auth);
        var categoria = repo.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        if (transacoes.existsByCategoriaId(id))
            throw new ResourceConflictException("Não é possível excluir uma categoria que possui transações vinculadas.");
        repo.delete(categoria);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication auth, Long id) {
        if (auth == null) return false;
        return !repo.existsById(id) || repo.existsByIdAndUsuarioId(id, usuarioService.usuarioAtual(auth).getId());
    }

    private CategoriaDTO dto(Categoria categoria) {
        return new CategoriaDTO(categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.getTipo());
    }
}
