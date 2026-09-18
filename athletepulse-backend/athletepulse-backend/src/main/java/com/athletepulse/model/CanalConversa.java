package com.athletepulse.model;

/**
 * Canais de conversa disponíveis para um atleta.
 * <p>
 * Cada atleta possui, no máximo, uma {@link Conversa} por canal - uma com
 * a comissão técnica e outra com a psicologia. As duas são completamente
 * isoladas entre si.
 */
public enum CanalConversa {
    /** Conversa entre o atleta e a comissão técnica. */
    COMISSAO,
    /** Conversa entre o atleta e a psicologia. */
    PSICOLOGO
}
