package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Resumo de uma conversa para a caixa de entrada da equipe (comissão ou psicologia).
 *
 * @param atletaId          identificador do atleta
 * @param atletaNome        nome do atleta
 * @param ultimaMensagem    texto da última mensagem trocada, ou {@code null} se {@code semMensagens}
 * @param ultimaMensagemEm  data e hora da última mensagem, ou {@code null} se {@code semMensagens}
 * @param ultimaFoiDoAtleta {@code true} se a última mensagem foi enviada pelo atleta (e não pela equipe)
 * @param semMensagens      {@code true} se ainda não há nenhuma mensagem trocada com esse atleta
 */
public record ConversaResumoResponse(
        Long atletaId,
        String atletaNome,
        String ultimaMensagem,
        LocalDateTime ultimaMensagemEm,
        boolean ultimaFoiDoAtleta,
        boolean semMensagens
) {}
