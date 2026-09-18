package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma notificação interna do sistema para um usuário.
 * <p>
 * Notificações são geradas automaticamente pelo próprio sistema em resposta
 * a eventos - uma nova mensagem, um treino atribuído, um atleta entrando em
 * alerta - nunca criadas diretamente por um usuário.
 */
@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuário que deve receber esta notificação. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    /** Categoria do evento que gerou a notificação. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacao tipo;

    /** Título curto, exibido em destaque na lista de notificações. */
    @Column(nullable = false, length = 150)
    private String titulo;

    /** Descrição da notificação. */
    @Column(nullable = false, length = 300)
    private String mensagem;

    /** Caminho relativo (ex: "painel-jogador.html") para onde o clique na notificação deve levar. */
    @Column(nullable = false, length = 200)
    private String link;

    /** Se a notificação já foi vista/aberta pelo usuário. */
    @Column(nullable = false)
    private boolean lida = false;

    /** Data e hora em que a notificação foi gerada. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();
}
