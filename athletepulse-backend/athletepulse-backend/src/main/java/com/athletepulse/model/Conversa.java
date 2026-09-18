package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma conversa entre um atleta e um canal
 * ({@link CanalConversa#COMISSAO} ou {@link CanalConversa#PSICOLOGO}).
 * <p>
 * Funciona como um "cabeçalho" de conversa: as mensagens em si ficam em
 * {@link Mensagem}. A constraint única em (atleta, canal) garante que só
 * exista uma conversa por combinação atleta+canal - ela é criada sob
 * demanda na primeira mensagem trocada (ver {@link com.athletepulse.service.ConversaService}).
 */
@Entity
@Table(
        name = "conversas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "canal"})
)
@Getter
@Setter
@NoArgsConstructor
public class Conversa {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Atleta dono desta conversa. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    /** Canal ao qual esta conversa pertence (comissão ou psicologia). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalConversa canal;

    /** Data e hora em que a conversa foi criada (na primeira mensagem enviada). */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();
}
