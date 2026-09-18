package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados ao criar uma nova mensagem, tanto pelo atleta quanto pela equipe.
 *
 * @param texto conteúdo da mensagem (até 2000 caracteres)
 */
public record MensagemRequest(
        @NotBlank(message = "Escreva uma mensagem")
        @Size(max = 2000, message = "Mensagem deve ter até 2000 caracteres")
        String texto
) {}
