package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Representação de uma nota psicológica para exibição.
 *
 * @param id        identificador da nota
 * @param texto     conteúdo da nota
 * @param autor     nome do psicólogo que escreveu a nota
 * @param criadoEm  data e hora de criação
 */
public record NotaResponse(
        Long id,
        String texto,
        String autor,
        LocalDateTime criadoEm
) {}
