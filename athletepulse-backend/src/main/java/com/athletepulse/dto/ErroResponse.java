package com.athletepulse.dto;

/**
 * Formato padrão de resposta de erro da API.
 * <p>
 * O front-end (ver {@code cadastro.js}, {@code login.js} e demais scripts)
 * lê o campo {@code mensagem} para exibir o erro ao usuário - o nome do
 * campo não deve ser alterado sem atualizar o front correspondente.
 *
 * @param mensagem descrição do erro, em português, pronta para exibição
 */
public record ErroResponse(String mensagem) {}
