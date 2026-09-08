package com.athletepulse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela comissão técnica para atribuir um treino do catálogo a um atleta, para hoje.
 *
 * @param atletaId    identificador do atleta
 * @param treinoId    identificador do treino do catálogo
 * @param observacoes observações opcionais específicas dessa atribuição
 */
public record AtribuirTreinoRequest(
        @NotNull(message = "Selecione o atleta")
        Long atletaId,

        @NotNull(message = "Selecione o treino")
        Long treinoId,

        @Size(max = 500, message = "Observações devem ter até 500 caracteres")
        String observacoes
) {}
