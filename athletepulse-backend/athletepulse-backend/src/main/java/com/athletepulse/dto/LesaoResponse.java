package com.athletepulse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representação de uma lesão para exibição.
 *
 * @param id              identificador da lesão
 * @param descricao       descrição da lesão
 * @param gravidade       gravidade, em minúsculas ("leve" | "moderada" | "grave")
 * @param status          status de liberação, em minúsculas ("em_avaliacao" | "em_recuperacao" | "liberado_com_restricao" | "liberado_total")
 * @param dataOcorrencia  data em que a lesão ocorreu/foi relatada
 * @param previsaoRetorno previsão de retorno, se houver
 * @param observacoes     observações do departamento médico
 * @param medicoNome      nome de quem fez a última atualização
 * @param criadoEm        data e hora de criação do registro
 * @param atualizadoEm    data e hora da última atualização
 */
public record LesaoResponse(
        Long id,
        String descricao,
        String gravidade,
        String status,
        LocalDate dataOcorrencia,
        LocalDate previsaoRetorno,
        String observacoes,
        String medicoNome,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
