package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa o treino atribuído a um atleta em um dia específico.
 * <p>
 * A constraint única em (atleta, data) garante um treino "vigente" por
 * atleta por dia. Diferente do {@link CheckIn}, essa atribuição pode ser
 * sobrescrita pela comissão técnica no mesmo dia (ex: o atleta piorou depois
 * do check-in e o treino planejado precisa mudar) - ver
 * {@code TreinoService.atribuir}.
 */
@Entity
@Table(
        name = "treinos_atribuidos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "data"})
)
@Getter
@Setter
@NoArgsConstructor
public class TreinoAtribuido {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Treino do catálogo atribuído. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false)
    private Treino treino;

    /** Atleta que deve realizar este treino. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    /** Membro da comissão técnica que fez a atribuição (ou a última atualização dela). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atribuido_por_id", nullable = false)
    private Usuario atribuidoPor;

    /** Dia a que esta atribuição se refere. */
    @Column(nullable = false)
    private LocalDate data = LocalDate.now();

    /** Observações opcionais da comissão sobre esta atribuição específica. */
    @Column(length = 500)
    private String observacoes;

    /** Data e hora da última atualização desta atribuição (criação ou troca de treino no mesmo dia). */
    @Column(nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
