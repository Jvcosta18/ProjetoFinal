package com.athletepulse.repository;

import com.athletepulse.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso a dados de {@link Notificacao}.
 */
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    /** Lista as notificações mais recentes de um usuário, mais nova primeiro. */
    List<Notificacao> findTop30ByDestinatario_IdOrderByCriadaEmDesc(Long destinatarioId);
    /** Conta quantas notificações não lidas um usuário tem. */
    long countByDestinatario_IdAndLidaFalse(Long destinatarioId);
}
