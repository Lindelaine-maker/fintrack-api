package br.com.fintrack.controller;

import br.com.fintrack.dto.*;
import br.com.fintrack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro de usuários e emissão de tokens JWT")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar usuário", description = "Cria uma conta e retorna um JWT para autenticação.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário cadastrado e token emitido"),
        @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos"),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    public ResponseEntity<TokenDTO> register(@Valid @RequestBody RegisterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais e retorna um JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticação realizada"),
        @ApiResponse(responseCode = "400", description = "Formato dos dados inválido"),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(service.login(dto));
    }
}
