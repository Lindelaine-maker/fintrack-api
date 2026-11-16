package br.com.fintrack.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name="RegisterDTO", description="Dados necessários para criação de uma conta")
public record RegisterDTO(
    @Schema(description="Nome do usuário", example="Ana") @NotBlank @Size(max=100) String nome,
    @Schema(description="E-mail único da conta", example="ana@email.com") @NotBlank @Email @Size(max=150) String email,
    @Schema(description="Senha com no mínimo 8 caracteres", example="Senha123") @NotBlank @Size(min=8,max=100) String senha
) {}
