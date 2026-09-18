package com.athletepulse.dto;

/**
 * Resumo do estado emocional de um atleta, usado na listagem do painel do psicólogo.
 * <p>
 * Contém apenas informação emocional - dados físicos (dor, fadiga) não são
 * expostos aqui, pois pertencem ao domínio da comissão técnica.
 *
 * @param id                     identificador do atleta
 * @param nome                   nome do atleta
 * @param email                  e-mail do atleta
 * @param status                 classificação calculada: {@code "ok"}, {@code "atencao"}, {@code "alerta"} ou {@code "sem_dado"}
 * @param ultimoEstadoEmocional  valor de 1 a 5 do último check-in enviado hoje, ou {@code null} se não houver
 * @param dataUltimoCheckin      data (ISO) do último check-in, ou {@code null} se nunca enviou nenhum
 * @param quedaConsecutiva       {@code true} se o atleta esteve emocionalmente em "atenção" ou
 *                               "alerta" nos últimos 3 dias consecutivos (sem falhar nenhum check-in)
 */
public record AtletaEmocionalResponse(
        Long id,
        String nome,
        String email,
        // "ok" | "atencao" | "alerta" | "sem_dado"
        String status,
        Integer ultimoEstadoEmocional,
        String dataUltimoCheckin,
        boolean quedaConsecutiva
) {}
