package com.athletepulse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representação de um check-in para exibição (histórico do atleta, visão da comissão, etc.).
 *
 * @param id              identificador do check-in
 * @param dataCheckin     data a que o check-in se refere
 * @param horasSono       horas de sono relatadas
 * @param fadiga          nível de fadiga (1 a 5)
 * @param estadoEmocional estado emocional (1 a 5)
 * @param temDor          se há dor/desconforto relatado
 * @param localDor        local da dor, se houver
 * @param intensidadeDor  intensidade da dor (1 a 5), se houver
 * @param observacoes     observações livres do atleta
 * @param criadoEm        data e hora em que o registro foi criado
 */
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
