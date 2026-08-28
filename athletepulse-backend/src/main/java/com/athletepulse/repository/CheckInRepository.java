package com.athletepulse.repository;

import com.athletepulse.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link CheckIn}.
 */
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    /** Lista todos os check-ins de um atleta, do mais recente para o mais antigo. */
    List<CheckIn> findByAtleta_IdOrderByDataCheckinDesc(Long atletaId);
    /** Verifica se o atleta já enviou um check-in numa determinada data (regra de "1 por dia"). */
    boolean existsByAtleta_IdAndDataCheckin(Long atletaId, LocalDate data);
    /** Busca o check-in mais recente de um atleta, se existir. */
    Optional<CheckIn> findFirstByAtleta_IdOrderByDataCheckinDesc(Long atletaId);
}
