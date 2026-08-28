package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Representação de uma mensagem para exibição no chat.
 *
 * @param id             identificador da mensagem
 * @param texto          conteúdo da mensagem
 * @param autorNome      nome de quem enviou
 * @param autorTipo      perfil de quem enviou ({@link com.athletepulse.model.TipoUsuario})
 * @param minhaMensagem  {@code true} se o autor da mensagem é o mesmo usuário que está
 *                       consultando a conversa - usado pelo front para alinhar a bolha
 *                       de chat à direita (enviada) ou à esquerda (recebida)
 * @param enviadaEm      data e hora de envio
 */
public record MensagemResponse(
        Long id,
        String texto,
        String autorNome,
        String autorTipo,
        boolean minhaMensagem,
        LocalDateTime enviadaEm
) {}
