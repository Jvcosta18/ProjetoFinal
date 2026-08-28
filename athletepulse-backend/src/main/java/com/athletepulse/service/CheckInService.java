package com.athletepulse.service;

import com.athletepulse.dto.AtletaResumoResponse;
import com.athletepulse.dto.CheckInRequest;
import com.athletepulse.dto.CheckInResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CheckIn;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.CheckInRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio dos check-ins diários dos atletas.
 * <p>
 * Também calcula, para a comissão técnica, uma classificação de risco por
 * atleta a partir do check-in mais recente ({@link #listarElenco}).
 */
@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UsuarioRepository usuarioRepository;

    public CheckInService(CheckInRepository checkInRepository, UsuarioRepository usuarioRepository) {
        this.checkInRepository = checkInRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra o check-in diário de um atleta.
     *
     * @param emailAutenticado e-mail do usuário autenticado (deve ser um atleta)
     * @param req              dados do check-in
     * @return o check-in criado
     * @throws NegocioException se o usuário não for atleta (403), já tiver
     *                          enviado check-in hoje (409), ou tiver informado
     *                          dor sem intensidade (400)
     */
    @Transactional
    public CheckInResponse registrar(String emailAutenticado, CheckInRequest req) {
        Usuario atleta = buscarAtleta(emailAutenticado);

        if (checkInRepository.existsByAtleta_IdAndDataCheckin(atleta.getId(), LocalDate.now())) {
            throw new NegocioException("Você já enviou o check-in de hoje.", HttpStatus.CONFLICT);
        }

        if (Boolean.TRUE.equals(req.temDor()) && (req.intensidadeDor() == null)) {
            throw new NegocioException("Informe a intensidade da dor.", HttpStatus.BAD_REQUEST);
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setAtleta(atleta);
        checkIn.setHorasSono(req.horasSono());
        checkIn.setFadiga(req.fadiga());
        checkIn.setEstadoEmocional(req.estadoEmocional());
        checkIn.setTemDor(Boolean.TRUE.equals(req.temDor()));
        checkIn.setLocalDor(checkIn.isTemDor() ? req.localDor() : null);
        checkIn.setIntensidadeDor(checkIn.isTemDor() ? req.intensidadeDor() : null);
        checkIn.setObservacoes(req.observacoes());

        checkInRepository.save(checkIn);
        return paraResponse(checkIn);
    }

    /**
     * Lista o histórico de check-ins do próprio atleta autenticado, do mais recente ao mais antigo.
     *
     * @param emailAutenticado e-mail do atleta
     * @throws NegocioException se o usuário não for atleta (403)
     */
    public List<CheckInResponse> listarMeus(String emailAutenticado) {
        Usuario atleta = buscarAtleta(emailAutenticado);
        return checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atleta.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Lista todos os atletas com um resumo do último check-in e a
     * classificação de risco calculada, para a visão de elenco da comissão técnica.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @throws NegocioException se o usuário não for da comissão técnica (403)
     */
    public List<AtletaResumoResponse> listarElenco(String emailComissao) {
        Usuario chamador = usuarioRepository.findByEmail(emailComissao)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (chamador.getTipo() != TipoUsuario.COMISSAO) {
            throw new NegocioException("Apenas a comissão técnica pode ver o elenco.", HttpStatus.FORBIDDEN);
        }

        List<Usuario> atletas = usuarioRepository.findByTipoOrderByNomeAsc(TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> {
                    CheckIn ultimo = checkInRepository
                            .findFirstByAtleta_IdOrderByDataCheckinDesc(atleta.getId())
                            .orElse(null);

                    return new AtletaResumoResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            atleta.getEmail(),
                            calcularStatus(ultimo),
                            ultimo != null ? paraResponse(ultimo) : null
                    );
                })
                .toList();
    }

    /**
     * Calcula a classificação de risco de um atleta a partir do último check-in:
     * <ul>
     *   <li>{@code sem_checkin} - não enviou check-in hoje (ou nunca enviou)</li>
     *   <li>{@code alerta} - dor com intensidade 4 ou 5</li>
     *   <li>{@code atencao} - há dor (qualquer intensidade), fadiga baixa (≤2) ou emocional baixo (≤2)</li>
     *   <li>{@code ok} - nenhum dos critérios acima</li>
     * </ul>
     */
    private String calcularStatus(CheckIn ultimo) {
        if (ultimo == null || !ultimo.getDataCheckin().isEqual(LocalDate.now())) {
            return "sem_checkin";
        }

        boolean dorForte = ultimo.isTemDor() && ultimo.getIntensidadeDor() != null && ultimo.getIntensidadeDor() >= 4;
        if (dorForte) {
            return "alerta";
        }

        boolean precisaAtencao = ultimo.isTemDor() || ultimo.getFadiga() <= 2 || ultimo.getEstadoEmocional() <= 2;
        return precisaAtencao ? "atencao" : "ok";
    }

    /** Busca o usuário pelo e-mail e garante que seu perfil é {@link TipoUsuario#JOGADOR}. */
    private Usuario buscarAtleta(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Apenas atletas podem enviar check-in.", HttpStatus.FORBIDDEN);
        }

        return usuario;
    }

    /** Converte a entidade {@link CheckIn} no DTO de resposta. */
    private CheckInResponse paraResponse(CheckIn c) {
        return new CheckInResponse(
                c.getId(),
                c.getDataCheckin(),
                c.getHorasSono(),
                c.getFadiga(),
                c.getEstadoEmocional(),
                c.isTemDor(),
                c.getLocalDor(),
                c.getIntensidadeDor(),
                c.getObservacoes(),
                c.getCriadoEm()
        );
    }
}
