package com.athletepulse.model;

/**
 * Status de liberação médica de uma {@link Lesao}.
 * <p>
 * Enquanto o status não for {@link #LIBERADO_TOTAL}, a comissão técnica não
 * consegue atribuir treinos de intensidade moderada ou intensa ao atleta
 * (ver {@code TreinoService.atribuir}) - apenas treinos de recuperação.
 */
public enum StatusLesao {
    /** Lesão relatada, aguardando avaliação do departamento médico. */
    EM_AVALIACAO,
    /** Em tratamento/recuperação - treino intenso bloqueado. */
    EM_RECUPERACAO,
    /** Pode treinar, mas com restrições (ex: intensidade reduzida) - segue bloqueando treino intenso. */
    LIBERADO_COM_RESTRICAO,
    /** Totalmente recuperado - libera o atleta para qualquer treino normalmente. */
    LIBERADO_TOTAL
}
