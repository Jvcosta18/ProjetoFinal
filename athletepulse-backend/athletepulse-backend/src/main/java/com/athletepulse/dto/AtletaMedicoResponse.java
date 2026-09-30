package com.athletepulse.dto;

/**
 * Resumo de um atleta para a visão do departamento médico.
 *
 * @param id               identificador do atleta
 * @param nome             nome do atleta
 * @param email            e-mail do atleta
 * @param statusLiberacao  status da lesão mais recente, em minúsculas, ou {@code "sem_registro"}
 *                         se o atleta nunca teve nenhuma lesão registrada (equivale a totalmente liberado)
 * @param descricaoLesao   descrição da lesão mais recente, ou {@code null} se não houver nenhuma
 */
public record AtletaMedicoResponse(
        Long id,
        String nome,
        String email,
        String statusLiberacao,
        String descricaoLesao
) {}
