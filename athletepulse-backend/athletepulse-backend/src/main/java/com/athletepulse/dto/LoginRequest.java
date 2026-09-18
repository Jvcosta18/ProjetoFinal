package com.athletepulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados enviados pelo front-end para autenticar um usuário.
 *
 * @param email  e-mail cadastrado
 * @param senha  senha em texto puro (comparada com o hash BCrypt salvo)
 * @param perfil perfil declarado na tela de login ("jogador" | "comissao" | "psicologo"),
 *               vindo do atributo {@code data-role} do formulário. Deve bater com o
 *               {@link com.athletepulse.model.TipoUsuario} real do usuário, ou o login falha.
 */
public record LoginRequest(
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @NotBlank(message = "Perfil é obrigatório")
        String perfil
) {}
