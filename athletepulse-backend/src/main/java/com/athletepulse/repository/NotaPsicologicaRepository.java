package com.athletepulse.repository;

import com.athletepulse.model.NotaPsicologica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaPsicologicaRepository extends JpaRepository<NotaPsicologica, Long> {
    List<NotaPsicologica> findByAtleta_IdOrderByCriadoEmDesc(Long atletaId);
}
