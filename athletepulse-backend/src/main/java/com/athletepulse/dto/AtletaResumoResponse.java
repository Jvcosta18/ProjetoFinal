package com.athletepulse.dto;

public record AtletaResumoResponse(
        Long id,
        String nome,
        String email,
        // "ok" | "atencao" | "alerta" | "sem_checkin"
        String status,
        CheckInResponse ultimoCheckin
) {}
