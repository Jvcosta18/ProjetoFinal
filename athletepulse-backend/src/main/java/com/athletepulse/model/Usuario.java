package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa um usuário do sistema AthletePulse.
 * <p>
 * Um usuário pode ser um atleta, um membro da comissão técnica ou um
 * psicólogo (ver {@link TipoUsuario}). Independente do perfil, todos os
 * usuários compartilham essa mesma tabela e o mesmo fluxo de autenticação.
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do usuário. */
    @Column(nullable = false, length = 120)
    private String nome;

    /** E-mail do usuário, usado como identificador de login. Único no sistema. */
    @Column(nullable = false, unique = true, length = 160)
    private String email;

    /** Hash BCrypt da senha - nunca é armazenada em texto puro. */
    @Column(nullable = false)
    private String senha;

    /** Perfil de acesso do usuário (jogador, comissão técnica ou psicólogo). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    /** Data e hora em que o cadastro foi criado. Preenchido automaticamente. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
