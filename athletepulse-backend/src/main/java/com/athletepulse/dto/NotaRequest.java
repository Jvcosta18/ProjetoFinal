package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pelo psicólogo ao registrar uma nova nota sobre um atleta.
 *
 * @param texto conteúdo da nota (até 2000 caracteres)
 */
public record NotaRequest(
        @NotBlank(message = "Escreva algo na nota")
        @Size(max = 2000, message = "Nota deve ter até 2000 caracteres")
        String texto
) {}
