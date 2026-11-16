package br.com.fintrack.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name="LoginDTO", description="Credenciais para autenticação")
public record LoginDTO(
    @Schema(description="E-mail cadastrado", example="ana@email.com")
    @NotBlank @Email String email,
    @Schema(description="Senha da conta", example="Senha123")
    @NotBlank String senha
) {}
