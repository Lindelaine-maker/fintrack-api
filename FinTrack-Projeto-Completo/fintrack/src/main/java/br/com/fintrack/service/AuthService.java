package br.com.fintrack.service;
import br.com.fintrack.dto.*;
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
        if(usuarios.existsByEmail(dto.email().toLowerCase())) throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        Usuario u=new Usuario();u.setNome(dto.nome().trim());u.setEmail(dto.email().toLowerCase().trim());u.setSenha(encoder.encode(dto.senha()));
        usuarios.save(u); return token(u);
    }
    public TokenDTO login(LoginDTO dto){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email().toLowerCase(),dto.senha()));
        Usuario u=usuarios.findByEmail(dto.email().toLowerCase()).orElseThrow();
        return token(u);
    }
    private TokenDTO token(Usuario u){
        var principal= User.withUsername(u.getEmail()).password(u.getSenha()).roles("USER").build();
        return new TokenDTO(jwtService.generateToken(principal),"Bearer",u.getId(),u.getNome(),u.getEmail());
    }
}
