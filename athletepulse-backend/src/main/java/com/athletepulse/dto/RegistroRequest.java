package com.athletepulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pelo front-end para criar um novo usuário.
 * <p>
 * O campo {@code tipo} chega como string minúscula vindo do {@code <select>}
 * do formulário de cadastro ("jogador", "comissao" ou "psicologo") e é
 * convertido para o enum {@link com.athletepulse.model.TipoUsuario} dentro
 * de {@link com.athletepulse.service.AuthService}.
 *
 * @param nome  nome completo do usuário
 * @param email e-mail, usado como identificador de login
 * @param tipo  perfil desejado, em texto ("jogador" | "comissao" | "psicologo")
 * @param senha senha em texto puro (é convertida para hash BCrypt antes de salvar)
 */
public record RegistroRequest(
        @Size(min = 3, max = 120, message = "Nome deve ter pelo menos 3 caracteres")
        String nome,

        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        String senha
) {}
