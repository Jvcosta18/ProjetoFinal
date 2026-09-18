package com.athletepulse.model;

/**
 * Perfis de acesso disponíveis no AthletePulse.
 */
public enum TipoUsuario {
    /** Atleta - envia check-ins diários e conversa com comissão/psicologia. */
    JOGADOR,
    /** Comissão técnica (inclui técnico e preparador físico). */
    COMISSAO,
    /** Psicólogo(a) - acesso restrito, com notas privadas entre atleta e psicólogo. */
    PSICOLOGO
}
