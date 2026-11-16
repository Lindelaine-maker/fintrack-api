package br.com.fintrack.dto;
import br.com.fintrack.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Schema(description="Dados de receita ou despesa")
public record TransacaoDTO(Long id, @NotBlank @Size(max=160) String descricao,
    @NotNull @DecimalMin(value="0.01") @Digits(integer=12,fraction=2) BigDecimal valor,
    @NotNull LocalDate data, @NotNull TipoTransacao tipo, @NotNull Long categoriaId, String categoriaNome) {}
