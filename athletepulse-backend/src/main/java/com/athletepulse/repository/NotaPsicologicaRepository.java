package com.athletepulse.repository;

import com.athletepulse.model.NotaPsicologica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso a dados de {@link NotaPsicologica}.
 */
public interface NotaPsicologicaRepository extends JpaRepository<NotaPsicologica, Long> {
    /** Lista todas as notas de um atleta, da mais recente para a mais antiga. */
    List<NotaPsicologica> findByAtleta_IdOrderByCriadoEmDesc(Long atletaId);
}
