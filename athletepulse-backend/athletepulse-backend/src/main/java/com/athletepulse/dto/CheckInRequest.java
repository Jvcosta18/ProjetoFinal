package com.athletepulse.dto;

import jakarta.validation.constraints.*;

/**
 * Dados enviados pelo atleta ao registrar o check-in diário.
 *
 * @param horasSono       horas de sono na noite anterior (0 a 24)
 * @param fadiga          nível de fadiga muscular, de 1 (muito cansado) a 5 (muito disposto)
 * @param estadoEmocional estado emocional, de 1 (péssimo) a 5 (ótimo)
 * @param temDor          se o atleta está sentindo alguma dor ou desconforto
 * @param localDor        local da dor (obrigatório apenas quando {@code temDor} é verdadeiro, validado em {@link com.athletepulse.service.CheckInService})
 * @param intensidadeDor  intensidade da dor, de 1 (leve) a 5 (intensa) (idem, obrigatório apenas se houver dor)
 * @param observacoes     observações livres e opcionais
 */
public record CheckInRequest(
        @NotNull(message = "Informe as horas de sono")
        @DecimalMin(value = "0", message = "Horas de sono inválidas")
        @DecimalMax(value = "24", message = "Horas de sono inválidas")
        Double horasSono,

        @NotNull(message = "Informe seu nível de fadiga")
        @Min(value = 1, message = "Fadiga deve ser entre 1 e 5")
        @Max(value = 5, message = "Fadiga deve ser entre 1 e 5")
        Integer fadiga,

        @NotNull(message = "Informe seu estado emocional")
        @Min(value = 1, message = "Estado emocional deve ser entre 1 e 5")
        @Max(value = 5, message = "Estado emocional deve ser entre 1 e 5")
        Integer estadoEmocional,

        @NotNull(message = "Informe se sente dor")
        Boolean temDor,

        @Size(max = 120)
        String localDor,

        @Min(value = 1, message = "Intensidade deve ser entre 1 e 5")
        @Max(value = 5, message = "Intensidade deve ser entre 1 e 5")
        Integer intensidadeDor,

        @Size(max = 500, message = "Observações devem ter até 500 caracteres")
        String observacoes
) {}
