package com.athletepulse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representação de um treino atribuído a um atleta, para exibição.
 *
 * @param id             identificador da atribuição
 * @param atletaId       identificador do atleta
 * @param atletaNome     nome do atleta
 * @param treino         treino do catálogo atribuído
 * @param data           dia a que a atribuição se refere
 * @param observacoes    observações específicas dessa atribuição
 * @param atualizadoEm   data e hora da última atualização
 */
public record TreinoAtribuidoResponse(
        Long id,
        Long atletaId,
        String atletaNome,
        TreinoResponse treino,
        LocalDate data,
        String observacoes,
        LocalDateTime atualizadoEm
) {}
