package com.athletepulse.repository;

import com.athletepulse.model.CanalConversa;
import com.athletepulse.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link Conversa}.
 */
public interface ConversaRepository extends JpaRepository<Conversa, Long> {
    /** Busca a conversa de um atleta com um canal específico, se já existir. */
    Optional<Conversa> findByAtleta_IdAndCanal(Long atletaId, CanalConversa canal);
    /** Lista todas as conversas de um determinado canal (usado pela caixa de entrada da equipe). */
    List<Conversa> findByCanal(CanalConversa canal);
}
