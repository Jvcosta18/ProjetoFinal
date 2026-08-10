package com.athletepulse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        LocalDate dataCheckin,
        Double horasSono,
        Integer fadiga,
        Integer estadoEmocional,
        boolean temDor,
        String localDor,
        Integer intensidadeDor,
        String observacoes,
        LocalDateTime criadoEm
) {}
