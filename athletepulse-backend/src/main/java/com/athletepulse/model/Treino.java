package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa um treino no catálogo da comissão técnica.
 * <p>
 * É um modelo reutilizável: um mesmo {@link Treino} pode ser atribuído a
 * vários atletas, em vários dias diferentes, através de {@link TreinoAtribuido}.
 */
@Entity
@Table(name = "treinos")
@Getter
@Setter
@NoArgsConstructor
public class Treino {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do treino (ex: "Treino Intenso - Força"). */
    @Column(nullable = false, length = 120)
    private String titulo;

    /** Descrição/instruções do treino. */
    @Column(nullable = false, length = 2000)
    private String descricao;

    /** Intensidade do treino, usada para sugestão automática por status de risco. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntensidadeTreino intensidade;

    /** Membro da comissão técnica que cadastrou este treino no catálogo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    /** Data e hora de criação. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
