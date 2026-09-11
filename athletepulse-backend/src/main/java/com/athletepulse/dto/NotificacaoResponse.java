package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Representação de uma notificação para exibição.
 *
 * @param id        identificador da notificação
 * @param tipo      categoria do evento ("mensagem" | "treino" | "alerta_atleta")
 * @param titulo    título curto
 * @param mensagem  descrição
 * @param link      caminho relativo para onde navegar ao clicar
 * @param lida      se já foi vista
 * @param criadaEm  data e hora de criação
 */
public record NotificacaoResponse(
        Long id,
        String tipo,
        String titulo,
        String mensagem,
        String link,
        boolean lida,
        LocalDateTime criadaEm
) {}
