package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Representação de um treino do catálogo para exibição.
 *
 * @param id          identificador do treino
 * @param titulo      nome do treino
 * @param descricao   descrição/instruções
 * @param intensidade intensidade, em minúsculas ("recuperacao" | "leve" | "moderada" | "intensa")
 * @param criadoEm    data e hora de criação
 */
public record TreinoResponse(
        Long id,
        String titulo,
        String descricao,
        String intensidade,
        LocalDateTime criadoEm
) {}
