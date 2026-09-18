package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa um clube (organização) dentro do sistema.
 * <p>
 * O AthletePulse é multi-clube: cada {@link Usuario} pertence a exatamente
 * um clube, e todos os dados do sistema (atletas, check-ins, treinos,
 * mensagens, notas) são isolados por clube - um clube nunca enxerga dados de
 * outro. Um clube é criado por um membro da comissão técnica no cadastro; os
 * demais usuários (atletas, outros membros da comissão, psicólogos) entram
 * no mesmo clube informando o {@link #token} de convite no próprio cadastro.
 */
@Entity
@Table(name = "clubes")
@Getter
@Setter
@NoArgsConstructor
public class Clube {

    /** Identificador único gerado pelo banco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do clube. */
    @Column(nullable = false, length = 120)
    private String nome;

    /**
     * Código de convite único do clube. Compartilhado pelo criador do clube
     * com os demais membros (atletas, comissão, psicologia) para que eles se
     * cadastrem no mesmo clube. É reutilizável - não expira nem se invalida
     * após o primeiro uso.
     */
    @Column(nullable = false, unique = true, length = 12)
    private String token;

    /** Data e hora de criação do clube. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
