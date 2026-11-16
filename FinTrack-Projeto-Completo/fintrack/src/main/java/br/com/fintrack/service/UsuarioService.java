package br.com.fintrack.service;

import br.com.fintrack.exception.ResourceNotFoundException;
import br.com.fintrack.model.Usuario;
import br.com.fintrack.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Usuario usuarioAtual(Authentication auth) {
        return repository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    /**
     * Serializa alterações financeiras e de categorias do mesmo proprietário.
     * O bloqueio deve ser obtido dentro de uma transação ativa.
     */
    @Transactional
    public Usuario usuarioAtualComBloqueio(Authentication auth) {
        return repository.findByEmailForUpdate(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}