package br.com.fintrack.dto;
public record TokenDTO(String token, String tipo, Long usuarioId, String nome, String email) {}
