package br.com.fintrack.dto;
import java.math.BigDecimal;
public record SaldoDTO(BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal saldoAtual) {}
