package com.athletepulse.repository;

import com.athletepulse.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso a dados de {@link Clube}.
 */
public interface ClubeRepository extends JpaRepository<Clube, Long> {
    /** Busca um clube pelo código de convite (token). */
    Optional<Clube> findByToken(String token);
    /** Verifica se um código de convite já está em uso (usado ao gerar um novo token, evitando colisão). */
    boolean existsByToken(String token);
}
