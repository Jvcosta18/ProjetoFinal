package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma nota de acompanhamento psicológico sobre um atleta.
 * <p>
 * Notas são visíveis apenas para usuários com perfil {@link TipoUsuario#PSICOLOGO}
 * (ver {@link com.athletepulse.service.PsicologoService}) - nem a comissão técnica
 * nem o próprio atleta têm acesso a esse conteúdo. O histórico de notas de um
 * atleta é compartilhado entre todos os psicólogos da equipe, não isolado por
 * profissional individual.
 */
@Entity
@Table(name = "notas_psicologicas")
@Getter
@Setter
@NoArgsConstructor
public class NotaPsicologica {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Atleta ao qual esta nota se refere. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    /** Psicólogo autor da nota. Guardado para histórico, mas a leitura das notas não filtra por autor. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "psicologo_id", nullable = false)
    private Usuario psicologo;

    /** Conteúdo da nota (observação, registro de sessão, etc.). */
    @Column(nullable = false, length = 2000)
    private String texto;

    /** Data e hora em que a nota foi registrada. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
