package com.athletepulse.dto;

import java.time.LocalDateTime;

public record NotaResponse(
        Long id,
        String texto,
        String autor,
        LocalDateTime criadoEm
) {}
