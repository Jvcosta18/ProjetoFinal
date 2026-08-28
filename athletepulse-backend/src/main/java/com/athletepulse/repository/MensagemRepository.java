package com.athletepulse.repository;

import com.athletepulse.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link Mensagem}.
 */
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    /** Lista todas as mensagens de uma conversa, em ordem cronológica crescente. */
    List<Mensagem> findByConversa_IdOrderByEnviadaEmAsc(Long conversaId);
    /** Busca a última mensagem enviada numa conversa (usado na prévia da caixa de entrada). */
    Optional<Mensagem> findFirstByConversa_IdOrderByEnviadaEmDesc(Long conversaId);
}
