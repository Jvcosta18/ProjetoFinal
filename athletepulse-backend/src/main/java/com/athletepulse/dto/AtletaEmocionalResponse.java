package com.athletepulse.dto;

public record AtletaEmocionalResponse(
        Long id,
        String nome,
        String email,
        // "ok" | "atencao" | "alerta" | "sem_dado"
        String status,
        Integer ultimoEstadoEmocional,
        String dataUltimoCheckin
) {}
