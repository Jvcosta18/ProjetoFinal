package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma única mensagem dentro de uma {@link Conversa}.
 */
@Entity
@Table(name = "mensagens")
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Conversa à qual esta mensagem pertence. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    /** Usuário que enviou a mensagem (pode ser o atleta ou alguém da equipe). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    /** Conteúdo da mensagem. */
    @Column(nullable = false, length = 2000)
    private String texto;

    /** Data e hora de envio da mensagem. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime enviadaEm = LocalDateTime.now();
}
