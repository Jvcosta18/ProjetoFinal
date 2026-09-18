package com.athletepulse.repository;

import com.athletepulse.model.TreinoAtribuido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link TreinoAtribuido}.
 */
public interface TreinoAtribuidoRepository extends JpaRepository<TreinoAtribuido, Long> {
    /** Busca a atribuição de um atleta numa data específica, se existir. */
    Optional<TreinoAtribuido> findByAtleta_IdAndData(Long atletaId, LocalDate data);
    /** Lista o histórico de treinos atribuídos a um atleta, do mais recente ao mais antigo. */
    List<TreinoAtribuido> findByAtleta_IdOrderByDataDesc(Long atletaId);
    /** Verifica se um treino do catálogo já foi atribuído a algum atleta alguma vez (usado para impedir exclusão indevida). */
    boolean existsByTreino_Id(Long treinoId);
}
