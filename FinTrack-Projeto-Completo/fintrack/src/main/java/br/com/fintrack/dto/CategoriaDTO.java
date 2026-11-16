package br.com.fintrack.dto;
import br.com.fintrack.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description="Categoria pertencente ao usuário autenticado")
public record CategoriaDTO(Long id, @NotBlank @Size(max=80) String nome,
                           @Size(max=200) String descricao, @NotNull TipoTransacao tipo) {}
