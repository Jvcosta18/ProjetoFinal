package com.athletepulse.dto;

import jakarta.validation.constraints.*;

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
