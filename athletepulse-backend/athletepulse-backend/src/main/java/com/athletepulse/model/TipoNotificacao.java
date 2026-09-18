package com.athletepulse.model;

/**
 * Categorias de evento que podem gerar uma {@link Notificacao}.
 */
public enum TipoNotificacao {
    /** Nova mensagem recebida numa conversa. */
    MENSAGEM,
    /** Um novo treino foi atribuído ao atleta. */
    TREINO,
    /** Um atleta entrou em status de alerta (físico ou emocional). */
    ALERTA_ATLETA
}
