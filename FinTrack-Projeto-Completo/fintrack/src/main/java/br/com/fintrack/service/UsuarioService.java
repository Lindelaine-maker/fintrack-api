package br.com.fintrack.service;
import br.com.fintrack.exception.ResourceNotFoundException;
import br.com.fintrack.model.Usuario;
import br.com.fintrack.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    public UsuarioService(UsuarioRepository repository){this.repository=repository;}
    public Usuario usuarioAtual(Authentication auth){
        return repository.findByEmail(auth.getName()).orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado."));
    }
}
