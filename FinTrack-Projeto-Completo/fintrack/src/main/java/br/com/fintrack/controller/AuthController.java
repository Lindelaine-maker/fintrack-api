package br.com.fintrack.controller;
import br.com.fintrack.dto.*;
import br.com.fintrack.service.AuthService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @Tag(name="Autenticação")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/register") @Operation(summary="Cadastrar usuário")
    public ResponseEntity<TokenDTO> register(@Valid @RequestBody RegisterDTO dto){return ResponseEntity.status(HttpStatus.CREATED).body(service.register(dto));}
    @PostMapping("/login") @Operation(summary="Autenticar e obter JWT")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto){return ResponseEntity.ok(service.login(dto));}
}
