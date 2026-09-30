package com.athletepulse.repository;

import com.athletepulse.model.Lesao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link Lesao}.
 */
public interface LesaoRepository extends JpaRepository<Lesao, Long> {
    /** Lista o histórico de lesões de um atleta, da mais recente à mais antiga. */
    List<Lesao> findByAtleta_IdOrderByCriadoEmDesc(Long atletaId);
    /** Busca a lesão mais recente de um atleta, se existir - determina sua liberação atual. */
    Optional<Lesao> findFirstByAtleta_IdOrderByCriadoEmDesc(Long atletaId);
}
