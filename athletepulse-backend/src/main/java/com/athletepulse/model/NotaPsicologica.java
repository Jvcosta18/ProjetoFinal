package com.athletepulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notas_psicologicas")
@Getter
@Setter
@NoArgsConstructor
public class NotaPsicologica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Usuario atleta;

    // Guardamos qual psicólogo escreveu, mas a consulta de notas de um atleta
    // não filtra por psicólogo específico - a equipe de psicologia compartilha
    // o histórico entre si (mais realista que notas isoladas por profissional).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "psicologo_id", nullable = false)
    private Usuario psicologo;

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
