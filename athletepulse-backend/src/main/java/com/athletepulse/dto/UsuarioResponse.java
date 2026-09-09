package com.athletepulse.dto;

import java.time.LocalDateTime;

/**
 * Representação de um usuário para exibição (perfil próprio ou gestão de contas).
 *
 * @param id       identificador do usuário
 * @param nome     nome completo
 * @param email    e-mail (identificador de login)
 * @param tipo     perfil, em minúsculas ("jogador" | "comissao" | "psicologo")
 * @param ativo    se a conta está ativa (pode fazer login)
 * @param criadoEm data e hora de criação do cadastro
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String tipo,
        boolean ativo,
        LocalDateTime criadoEm
) {}
