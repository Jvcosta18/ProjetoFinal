package com.athletepulse.repository;

import com.athletepulse.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso a dados de {@link Treino} (catálogo).
 */
public interface TreinoRepository extends JpaRepository<Treino, Long> {
    /** Lista todo o catálogo de treinos, do mais recente ao mais antigo. */
    List<Treino> findAllByOrderByCriadoEmDesc();
}
