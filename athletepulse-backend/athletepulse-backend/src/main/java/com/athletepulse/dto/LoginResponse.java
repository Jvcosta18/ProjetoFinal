package com.athletepulse.dto;

/**
 * Resposta do login: token de autenticação e dados básicos do usuário.
 *
 * @param token token JWT a ser enviado em {@code Authorization: Bearer <token>} nas próximas requisições
 * @param id    identificador do usuário autenticado
 * @param nome  nome do usuário autenticado (exibido no topo dos painéis)
 * @param tipo  perfil do usuário, em minúsculas (usado pelo front para decidir qual painel abrir)
 */
public record LoginResponse(
        String token,
        Long id,
        String nome,
        String tipo
) {}
