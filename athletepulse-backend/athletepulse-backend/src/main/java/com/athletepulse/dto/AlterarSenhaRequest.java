package com.athletepulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pelo próprio usuário para trocar sua senha.
 * <p>
 * Exige a senha atual como confirmação - evita que alguém com uma sessão
 * aberta sem supervisão consiga trocar a senha sem saber a original.
 *
 * @param senhaAtual senha atual, para confirmação
 * @param novaSenha  nova senha desejada
 */
public record AlterarSenhaRequest(
        @NotBlank(message = "Informe sua senha atual")
        String senhaAtual,

        @Size(min = 6, message = "Nova senha deve ter pelo menos 6 caracteres")
        String novaSenha
) {}
