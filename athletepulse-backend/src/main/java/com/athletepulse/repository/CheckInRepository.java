package com.athletepulse.repository;

import com.athletepulse.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByAtleta_IdOrderByDataCheckinDesc(Long atletaId);
    boolean existsByAtleta_IdAndDataCheckin(Long atletaId, LocalDate data);
}
