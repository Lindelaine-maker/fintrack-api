package br.com.fintrack.dto;
import br.com.fintrack.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name="CategoriaDTO", description="Categoria de receita ou despesa pertencente ao usuário autenticado")
public record CategoriaDTO(
    @Schema(accessMode=Schema.AccessMode.READ_ONLY, description="Identificador da categoria") Long id,
    @Schema(description="Nome da categoria", example="Alimentação") @NotBlank @Size(max=80) String nome,
    @Schema(description="Descrição opcional", example="Mercado e refeições") @Size(max=200) String descricao,
    @Schema(description="Tipo de lançamento", example="DESPESA") @NotNull TipoTransacao tipo
) {}
