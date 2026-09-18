package com.athletepulse.model;

/**
 * Níveis de intensidade de um treino.
 * <p>
 * Usado tanto para classificar treinos no catálogo quanto para calcular a
 * sugestão automática de intensidade a partir do status de risco do atleta
 * (ver {@code TreinoService.sugerirIntensidade}).
 */
public enum IntensidadeTreino {
    /** Treino leve, focado em recuperação - sugerido para atletas em alerta. */
    RECUPERACAO,
    /** Treino de intensidade reduzida - sugerido para atletas em atenção. */
    LEVE,
    /** Treino de intensidade padrão. */
    MODERADA,
    /** Treino de alta intensidade - sugerido para atletas sem restrições. */
    INTENSA
}
