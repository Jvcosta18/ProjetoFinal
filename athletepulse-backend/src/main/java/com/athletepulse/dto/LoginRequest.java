package com.athletepulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        // "jogador" ou "comissao" - vem do data-role do form no front.
        @NotBlank(message = "Perfil é obrigatório")
        String perfil
) {}
