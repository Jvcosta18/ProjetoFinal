package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados enviados pelo departamento médico ao registrar uma nova lesão/queixa de um atleta.
 *
 * @param descricao       descrição da lesão (ex: "Entorse de tornozelo direito")
 * @param gravidade       gravidade, em texto ("leve" | "moderada" | "grave")
 * @param status          status inicial de liberação, em texto ("em_avaliacao" | "em_recuperacao" | "liberado_com_restricao" | "liberado_total")
 * @param previsaoRetorno previsão de retorno aos treinos normais, opcional
 * @param observacoes     observações livres, opcional
 */
public record LesaoRequest(
        @NotBlank(message = "Descreva a lesão")
        @Size(max = 300, message = "Descrição deve ter até 300 caracteres")
        String descricao,

        @NotBlank(message = "Informe a gravidade")
        String gravidade,

        @NotBlank(message = "Informe o status")
        String status,

        LocalDate previsaoRetorno,

        @Size(max = 1000, message = "Observações devem ter até 1000 caracteres")
        String observacoes
) {}
