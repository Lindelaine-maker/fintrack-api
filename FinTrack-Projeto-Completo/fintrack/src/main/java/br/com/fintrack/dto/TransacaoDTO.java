package br.com.fintrack.dto;
import br.com.fintrack.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name="TransacaoDTO", description="Dados de uma receita ou despesa")
public record TransacaoDTO(
    @Schema(accessMode=Schema.AccessMode.READ_ONLY, description="Identificador da transação") Long id,
    @Schema(description="Descrição do lançamento", example="Salário mensal") @NotBlank @Size(max=160) String descricao,
    @Schema(description="Valor positivo com até duas casas decimais", example="3500.00") @NotNull @DecimalMin(value="0.01") @Digits(integer=12,fraction=2) BigDecimal valor,
    @Schema(description="Data do lançamento", example="2026-11-05") @NotNull LocalDate data,
    @Schema(description="Natureza do lançamento", example="RECEITA") @NotNull TipoTransacao tipo,
    @Schema(description="ID de categoria pertencente ao usuário autenticado", example="1") @NotNull Long categoriaId,
    @Schema(accessMode=Schema.AccessMode.READ_ONLY, description="Nome da categoria associada") String categoriaNome
) {}
