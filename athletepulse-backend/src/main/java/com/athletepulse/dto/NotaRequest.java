package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotaRequest(
        @NotBlank(message = "Escreva algo na nota")
        @Size(max = 2000, message = "Nota deve ter até 2000 caracteres")
        String texto
) {}
