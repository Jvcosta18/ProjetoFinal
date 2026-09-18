package com.athletepulse.dto;

import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela comissão técnica para redefinir a senha de um usuário
 * que esqueceu a própria senha (substitui um fluxo de e-mail de recuperação,
 * que exigiria um servidor de e-mail configurado).
 *
 * @param novaSenha nova senha temporária a ser informada ao usuário por fora do sistema
 */
public record AdminRedefinirSenhaRequest(
        @Size(min = 6, message = "Nova senha deve ter pelo menos 6 caracteres")
        String novaSenha
) {}
