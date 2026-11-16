package br.com.fintrack.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="TokenDTO", description="Token JWT retornado no cadastro e no login")
public record TokenDTO(
    @Schema(description="Token de acesso JWT") String token,
    @Schema(description="Esquema de autenticação", example="Bearer") String tipo,
    @Schema(description="Identificador do usuário autenticado") Long usuarioId,
    @Schema(description="Nome do usuário") String nome,
    @Schema(description="E-mail do usuário") String email
) {}
