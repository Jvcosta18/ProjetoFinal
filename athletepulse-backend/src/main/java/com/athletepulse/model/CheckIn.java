package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "checkins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "data_checkin"})
        // Regra de negócio: um check-in por atleta por dia.
)
@Getter
@Setter
@NoArgsConstructor
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    @Column(name = "data_checkin", nullable = false)
    private LocalDate dataCheckin = LocalDate.now();

    @Column(nullable = false)
    private Double horasSono;

    // Escala 1 (muito cansado) a 5 (muito disposto)
    @Column(nullable = false)
    private Integer fadiga;

    // Escala 1 (péssimo) a 5 (ótimo)
    @Column(name = "estado_emocional", nullable = false)
    private Integer estadoEmocional;

    @Column(name = "tem_dor", nullable = false)
    private boolean temDor;

    @Column(name = "local_dor")
    private String localDor;

    // Escala 1 (leve) a 5 (intensa) - só preenchido se temDor = true
    @Column(name = "intensidade_dor")
    private Integer intensidadeDor;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
