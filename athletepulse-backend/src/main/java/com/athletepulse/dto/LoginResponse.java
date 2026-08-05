package com.athletepulse.dto;

public record LoginResponse(
        String token,
        String nome,
        String tipo
) {}
