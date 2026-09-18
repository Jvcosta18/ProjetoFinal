package com.athletepulse.dto;

/**
 * Resposta do cadastro de um novo usuário.
 *
 * @param nomeClube  nome do clube ao qual o usuário passou a pertencer
 * @param tokenClube código de convite do clube, preenchido <b>apenas</b> quando
 *                   um clube novo foi criado nesse cadastro (comissão técnica
 *                   sem clube prévio) - o front deve exibir esse código com
 *                   destaque para a pessoa compartilhar com o resto da equipe.
 *                   Vem {@code null} quando o usuário entrou num clube já existente.
 */
public record RegistroResponse(
        String nomeClube,
        String tokenClube
) {}
