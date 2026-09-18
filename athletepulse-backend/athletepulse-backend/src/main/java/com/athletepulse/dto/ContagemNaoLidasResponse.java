package com.athletepulse.dto;

/**
 * Contagem de notificações não lidas do usuário autenticado.
 *
 * @param naoLidas quantidade de notificações ainda não vistas
 */
public record ContagemNaoLidasResponse(long naoLidas) {}
