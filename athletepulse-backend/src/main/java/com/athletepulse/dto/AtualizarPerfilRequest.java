package com.athletepulse.dto;

import jakarta.validation.constraints.Size;

/**
 * Dados enviados pelo próprio usuário para atualizar seu nome de exibição.
 *
 * @param nome novo nome completo
 */
public record AtualizarPerfilRequest(
        @Size(min = 3, max = 120, message = "Nome deve ter pelo menos 3 caracteres")
        String nome
) {}
