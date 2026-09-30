package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma lesão/queixa física de um atleta, avaliada
 * pelo departamento médico/fisioterapia.
 * <p>
 * O registro mais recente de um atleta determina sua liberação atual: se o
 * {@link #status} não for {@link StatusLesao#LIBERADO_TOTAL}, a comissão
 * técnica é impedida de atribuir treinos de intensidade moderada ou intensa
 * a esse atleta (ver {@code TreinoService}).
 */
@Entity
@Table(name = "lesoes")
@Getter
@Setter
@NoArgsConstructor
public class Lesao {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Atleta ao qual esta lesão se refere. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    /** Membro do departamento médico responsável pela última atualização deste registro. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuario medico;

    /** Descrição da lesão ou queixa (ex: "Entorse de tornozelo direito"). */
    @Column(nullable = false, length = 300)
    private String descricao;

    /** Gravidade avaliada pelo departamento médico. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GravidadeLesao gravidade;

    /** Status atual de liberação para treino. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusLesao status;

    /** Data em que a lesão ocorreu ou foi relatada. */
    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia = LocalDate.now();

    /** Previsão de retorno aos treinos normais, se estimada. */
    @Column(name = "previsao_retorno")
    private LocalDate previsaoRetorno;

    /** Observações livres do departamento médico. */
    @Column(length = 1000)
    private String observacoes;

    /** Data e hora de criação do registro. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    /** Data e hora da última atualização (mudança de status, por exemplo). */
    @Column(nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
