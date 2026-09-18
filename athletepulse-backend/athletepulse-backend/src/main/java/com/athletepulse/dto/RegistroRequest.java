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
 * <p>
 * O AthletePulse é multi-clube: todo usuário precisa pertencer a um clube.
 * Atletas e psicólogos sempre entram num clube já existente informando
 * {@code tokenClube}. A comissão técnica escolhe entre criar um clube novo
 * ({@code temClube = false}, informando {@code nomeClube}) ou entrar num já
 * existente ({@code temClube = true}, informando {@code tokenClube}) - ver
 * {@link com.athletepulse.service.AuthService#registrar}.
 *
 * @param nome       nome completo do usuário
 * @param email      e-mail, usado como identificador de login
 * @param tipo       perfil desejado, em texto ("jogador" | "comissao" | "psicologo")
 * @param senha      senha em texto puro (é convertida para hash BCrypt antes de salvar)
 * @param temClube   apenas para {@code tipo = "comissao"}: se já existe um clube pra entrar
 *                   (via token) ou se deve criar um novo (via nome). Ignorado para os demais tipos.
 * @param tokenClube código de convite do clube - obrigatório para jogador e psicólogo, e
 *                   para comissão quando {@code temClube = true}
 * @param nomeClube  nome do novo clube - obrigatório apenas para comissão quando {@code temClube = false}
 */
public record RegistroRequest(
        @Size(min = 3, max = 120, message = "Nome deve ter pelo menos 3 caracteres")
        String nome,

        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        String senha,

        Boolean temClube,

        String tokenClube,

        String nomeClube
) {}
