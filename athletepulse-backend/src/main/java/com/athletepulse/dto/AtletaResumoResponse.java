package com.athletepulse.dto;

/**
 * Resumo de um atleta para a visão de elenco da comissão técnica.
 *
 * @param id                  identificador do atleta
 * @param nome                nome do atleta
 * @param email               e-mail do atleta
 * @param status              classificação de risco calculada a partir do último check-in:
 *                            {@code "ok"}, {@code "atencao"}, {@code "alerta"} ou {@code "sem_checkin"}
 *                            (ver {@code CheckInService.calcularStatus})
 * @param ultimoCheckin       último check-in enviado pelo atleta, ou {@code null} se nunca enviou nenhum
 * @param intensidadeSugerida intensidade de treino sugerida a partir do {@code status}, em minúsculas
 *                            (ver {@code TreinoService.sugerirIntensidade}) - apenas uma sugestão,
 *                            a comissão pode atribuir qualquer treino do catálogo
 */
public record AtletaResumoResponse(
        Long id,
        String nome,
        String email,
        // "ok" | "atencao" | "alerta" | "sem_checkin"
        String status,
        CheckInResponse ultimoCheckin,
        String intensidadeSugerida
) {}
