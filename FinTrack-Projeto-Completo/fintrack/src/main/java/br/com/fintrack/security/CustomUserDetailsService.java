package br.com.fintrack.security;
import br.com.fintrack.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;
    public CustomUserDetailsService(UsuarioRepository repository){this.repository=repository;}
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u=repository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Usuário não encontrado"));
        return User.withUsername(u.getEmail()).password(u.getSenha()).roles("USER").build();
    }
}
