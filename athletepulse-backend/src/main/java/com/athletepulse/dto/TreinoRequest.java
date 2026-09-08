package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela comissão técnica para cadastrar um novo treino no catálogo.
 *
 * @param titulo      nome do treino
 * @param descricao   descrição/instruções do treino
 * @param intensidade intensidade, em texto ("recuperacao" | "leve" | "moderada" | "intensa")
 */
public record TreinoRequest(
        @NotBlank(message = "Informe o título do treino")
        @Size(max = 120, message = "Título deve ter até 120 caracteres")
        String titulo,

        @NotBlank(message = "Informe a descrição do treino")
        @Size(max = 2000, message = "Descrição deve ter até 2000 caracteres")
        String descricao,

        @NotBlank(message = "Informe a intensidade do treino")
        String intensidade
) {}
