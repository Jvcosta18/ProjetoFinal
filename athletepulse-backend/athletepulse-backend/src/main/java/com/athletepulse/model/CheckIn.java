package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa o check-in diário de um atleta.
 * <p>
 * Registra sono, fadiga, estado emocional e eventual dor/desconforto.
 * A constraint única em (atleta, data) garante a regra de negócio de que
 * cada atleta só pode enviar um check-in por dia - a validação também é
 * reforçada em {@link com.athletepulse.service.CheckInService}.
 */
@Entity
@Table(
        name = "checkins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "data_checkin"})
)
@Getter
@Setter
@NoArgsConstructor
public class CheckIn {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Atleta que enviou este check-in. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    /** Data a que este check-in se refere (não necessariamente igual à de criação). */
    @Column(name = "data_checkin", nullable = false)
    private LocalDate dataCheckin = LocalDate.now();

    /** Horas de sono na noite anterior ao check-in. */
    @Column(nullable = false)
    private Double horasSono;

    /** Nível de fadiga muscular, em escala de 1 (muito cansado) a 5 (muito disposto). */
    @Column(nullable = false)
    private Integer fadiga;

    /** Estado emocional do atleta, em escala de 1 (péssimo) a 5 (ótimo). */
    @Column(name = "estado_emocional", nullable = false)
    private Integer estadoEmocional;

    /** Indica se o atleta relatou alguma dor ou desconforto físico. */
    @Column(name = "tem_dor", nullable = false)
    private boolean temDor;

    /** Local da dor relatada (ex: "Joelho direito"). Nulo quando {@link #temDor} é falso. */
    @Column(name = "local_dor")
    private String localDor;

    /** Intensidade da dor, de 1 (leve) a 5 (intensa). Preenchido apenas se {@link #temDor} for verdadeiro. */
    @Column(name = "intensidade_dor")
    private Integer intensidadeDor;

    /** Observações livres e opcionais do atleta sobre o dia. */
    @Column(length = 500)
    private String observacoes;

    /** Data e hora em que o registro foi criado no sistema. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
