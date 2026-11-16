package br.com.fintrack.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name="SaldoDTO", description="Resumo de receitas, despesas e saldo do usuário autenticado")
public record SaldoDTO(
    @Schema(description="Soma de todas as receitas", example="3500.00") BigDecimal totalReceitas,
    @Schema(description="Soma de todas as despesas", example="450.00") BigDecimal totalDespesas,
    @Schema(description="Total de receitas menos total de despesas", example="3050.00") BigDecimal saldoAtual
) {}
