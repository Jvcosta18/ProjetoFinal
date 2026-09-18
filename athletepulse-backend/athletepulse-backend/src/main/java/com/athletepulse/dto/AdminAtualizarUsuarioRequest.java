package com.athletepulse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela comissão técnica para editar a conta de um usuário.
 *
 * @param nome  novo nome completo
 * @param ativo se a conta deve ficar ativa (pode logar) ou desativada
 */
public record AdminAtualizarUsuarioRequest(
        @Size(min = 3, max = 120, message = "Nome deve ter pelo menos 3 caracteres")
        String nome,

        @NotNull(message = "Informe o status da conta")
        Boolean ativo
) {}
