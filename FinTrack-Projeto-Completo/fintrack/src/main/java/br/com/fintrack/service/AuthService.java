package br.com.fintrack.service;
import br.com.fintrack.dto.*;
import br.com.fintrack.exception.ResourceConflictException;
import br.com.fintrack.model.Usuario;
import br.com.fintrack.repository.UsuarioRepository;
import br.com.fintrack.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios; private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager; private final JwtService jwtService;
    public AuthService(UsuarioRepository usuarios,PasswordEncoder encoder,AuthenticationManager authenticationManager,JwtService jwtService){
        this.usuarios=usuarios;this.encoder=encoder;this.authenticationManager=authenticationManager;this.jwtService=jwtService;
    }
    @Transactional
    public TokenDTO register(RegisterDTO dto){
        String email=dto.email().trim().toLowerCase();
        if(usuarios.existsByEmail(email)) throw new ResourceConflictException("Este e-mail já está cadastrado.");
        Usuario u=new Usuario();u.setNome(dto.nome().trim());u.setEmail(email);u.setSenha(encoder.encode(dto.senha()));
        usuarios.save(u); return token(u);
    }
    public TokenDTO login(LoginDTO dto){
        String email=dto.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,dto.senha()));
        Usuario u=usuarios.findByEmail(email).orElseThrow(()->new BadCredentialsException("Credenciais inválidas."));
        return token(u);
    }
    private TokenDTO token(Usuario u){
        var principal= User.withUsername(u.getEmail()).password(u.getSenha()).roles("USER").build();
        return new TokenDTO(jwtService.generateToken(principal),"Bearer",u.getId(),u.getNome(),u.getEmail());
    }
}
