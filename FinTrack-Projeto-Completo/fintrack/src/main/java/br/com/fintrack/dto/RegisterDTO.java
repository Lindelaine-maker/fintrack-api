package br.com.fintrack.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description="Dados para criação de conta")
public record RegisterDTO(@NotBlank @Size(max=100) String nome, @NotBlank @Email @Size(max=150) String email,
                          @NotBlank @Size(min=8,max=100) String senha) {}
