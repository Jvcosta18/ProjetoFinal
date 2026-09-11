package com.athletepulse.dto;

import java.time.LocalDate;

/**
 * Um ponto do histórico emocional de um atleta, para o gráfico de evolução
 * no painel do psicólogo. Contém apenas data e estado emocional - nenhum
 * dado físico (sono, fadiga, dor) é exposto aqui.
 *
 * @param data            dia do check-in
 * @param estadoEmocional valor de 1 a 5 relatado nesse dia
 */
public record PontoEmocionalResponse(
        LocalDate data,
        Integer estadoEmocional
) {}
