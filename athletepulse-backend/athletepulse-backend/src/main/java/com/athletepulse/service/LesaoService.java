package com.athletepulse.service;

import com.athletepulse.dto.AtletaMedicoResponse;
import com.athletepulse.dto.LesaoRequest;
import com.athletepulse.dto.LesaoResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.*;
import com.athletepulse.repository.LesaoRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio do departamento médico/fisioterapia: registro de lesões
 * e cálculo da liberação de cada atleta para treinar.
 * <p>
 * Enquanto a lesão mais recente de um atleta não estiver com status
 * {@link StatusLesao#LIBERADO_TOTAL}, a comissão técnica é impedida de
 * atribuir treinos de intensidade moderada ou intensa a ele - ver
 * {@link #exigirLiberacaoParaTreino}, usado por {@code TreinoService.atribuir}.
 */
@Service
public class LesaoService {

    private final LesaoRepository lesaoRepository;
    private final UsuarioRepository usuarioRepository;

    public LesaoService(LesaoRepository lesaoRepository, UsuarioRepository usuarioRepository) {
        this.lesaoRepository = lesaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista todos os atletas do clube do médico autenticado, com o status de
     * liberação calculado a partir da lesão mais recente de cada um.
     *
     * @param emailMedico e-mail do usuário autenticado (deve ser do departamento médico)
     * @throws NegocioException se o usuário não for do departamento médico (403)
     */
    public List<AtletaMedicoResponse> listarAtletas(String emailMedico) {
        Usuario medico = exigirMedico(emailMedico);

        List<Usuario> atletas = usuarioRepository.findByClube_IdAndTipoOrderByNomeAsc(medico.getClube().getId(), TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> {
                    Lesao ultima = lesaoRepository.findFirstByAtleta_IdOrderByCriadoEmDesc(atleta.getId()).orElse(null);

                    return new AtletaMedicoResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            atleta.getEmail(),
                            ultima != null ? ultima.getStatus().name().toLowerCase() : "sem_registro",
                            ultima != null ? ultima.getDescricao() : null
                    );
                })
                .toList();
    }

    /**
     * Lista o histórico de lesões de um atleta, da mais recente à mais antiga.
     *
     * @param emailMedico e-mail do usuário autenticado (deve ser do departamento médico)
     * @param atletaId    identificador do atleta
     * @throws NegocioException se o usuário não for do departamento médico (403) ou o atleta não existir/não for do mesmo clube (404)
     */
    public List<LesaoResponse> listarHistorico(String emailMedico, Long atletaId) {
        Usuario medico = exigirMedico(emailMedico);
        buscarAtletaPorId(atletaId, medico.getClube().getId());

        return lesaoRepository.findByAtleta_IdOrderByCriadoEmDesc(atletaId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Registra uma nova lesão/queixa para um atleta.
     *
     * @param emailMedico e-mail do usuário autenticado (deve ser do departamento médico)
     * @param atletaId    identificador do atleta
     * @param req         dados da lesão
     * @return a lesão criada
     * @throws NegocioException se o usuário não for do departamento médico (403), o
     *                          atleta não existir/não for do mesmo clube (404), ou
     *                          gravidade/status forem inválidos (400)
     */
    @Transactional
    public LesaoResponse registrar(String emailMedico, Long atletaId, LesaoRequest req) {
        Usuario medico = exigirMedico(emailMedico);
        Usuario atleta = buscarAtletaPorId(atletaId, medico.getClube().getId());

        Lesao lesao = new Lesao();
        lesao.setAtleta(atleta);
        lesao.setMedico(medico);
        preencher(lesao, req);

        lesaoRepository.save(lesao);
        return paraResponse(lesao);
    }

    /**
     * Atualiza uma lesão existente (ex: mudar o status conforme o atleta evolui na recuperação).
     *
     * @param emailMedico e-mail do usuário autenticado (deve ser do departamento médico)
     * @param lesaoId     identificador da lesão
     * @param req         novos dados
     * @return a lesão atualizada
     * @throws NegocioException se o usuário não for do departamento médico (403), a
     *                          lesão não existir/não for do mesmo clube (404), ou
     *                          gravidade/status forem inválidos (400)
     */
    @Transactional
    public LesaoResponse atualizar(String emailMedico, Long lesaoId, LesaoRequest req) {
        Usuario medico = exigirMedico(emailMedico);
        Lesao lesao = lesaoRepository.findById(lesaoId)
                .orElseThrow(() -> new NegocioException("Registro não encontrado.", HttpStatus.NOT_FOUND));

        if (!lesao.getAtleta().getClube().getId().equals(medico.getClube().getId())) {
            throw new NegocioException("Registro não encontrado.", HttpStatus.NOT_FOUND);
        }

        lesao.setMedico(medico);
        preencher(lesao, req);
        lesao.setAtualizadoEm(java.time.LocalDateTime.now());

        lesaoRepository.save(lesao);
        return paraResponse(lesao);
    }

    /**
     * Verifica se um atleta está liberado para um treino de determinada
     * intensidade, usado por {@code TreinoService.atribuir} antes de atribuir
     * um treino. Treinos de recuperação são sempre permitidos, independente
     * do status médico do atleta.
     *
     * @param atletaId    identificador do atleta
     * @param intensidade intensidade do treino que se deseja atribuir
     * @throws NegocioException com status 409 se o atleta não estiver liberado para essa intensidade
     */
    public void exigirLiberacaoParaTreino(Long atletaId, IntensidadeTreino intensidade) {
        if (intensidade == IntensidadeTreino.RECUPERACAO) {
            return; // treino de recuperação é sempre permitido, não precisa de liberação.
        }

        Optional<Lesao> ultima = lesaoRepository.findFirstByAtleta_IdOrderByCriadoEmDesc(atletaId);
        if (ultima.isEmpty()) {
            return; // atleta nunca teve lesão registrada - liberado.
        }

        StatusLesao status = ultima.get().getStatus();
        if (status != StatusLesao.LIBERADO_TOTAL) {
            throw new NegocioException(
                    "Este atleta não está liberado pelo departamento médico para esse tipo de treino. " +
                            "Atribua um treino de recuperação ou aguarde a liberação total.",
                    HttpStatus.CONFLICT
            );
        }
    }

    /** Preenche os campos de uma lesão a partir do DTO de requisição, validando gravidade e status. */
    private void preencher(Lesao lesao, LesaoRequest req) {
        lesao.setDescricao(req.descricao().trim());
        lesao.setGravidade(converterGravidade(req.gravidade()));
        lesao.setStatus(converterStatus(req.status()));
        lesao.setPrevisaoRetorno(req.previsaoRetorno());
        lesao.setObservacoes(req.observacoes());
    }

    private GravidadeLesao converterGravidade(String valor) {
        try {
            return GravidadeLesao.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Gravidade inválida.", HttpStatus.BAD_REQUEST);
        }
    }

    private StatusLesao converterStatus(String valor) {
        try {
            return StatusLesao.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Status inválido.", HttpStatus.BAD_REQUEST);
        }
    }

    /** Busca o usuário pelo e-mail e garante que seu perfil é {@link TipoUsuario#MEDICO}. */
    private Usuario exigirMedico(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != TipoUsuario.MEDICO) {
            throw new NegocioException("Apenas o departamento médico pode acessar essa área.", HttpStatus.FORBIDDEN);
        }

        return usuario;
    }

    /** Busca um usuário pelo id, garante que seu perfil é {@link TipoUsuario#JOGADOR} e que pertence ao clube informado. */
    private Usuario buscarAtletaPorId(Long id, Long clubeId) {
        Usuario atleta = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Atleta não encontrado.", HttpStatus.NOT_FOUND));

        if (atleta.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Usuário informado não é um atleta.", HttpStatus.BAD_REQUEST);
        }

        if (!atleta.getClube().getId().equals(clubeId)) {
            throw new NegocioException("Atleta não encontrado.", HttpStatus.NOT_FOUND);
        }

        return atleta;
    }

    private LesaoResponse paraResponse(Lesao l) {
        return new LesaoResponse(
                l.getId(),
                l.getDescricao(),
                l.getGravidade().name().toLowerCase(),
                l.getStatus().name().toLowerCase(),
                l.getDataOcorrencia(),
                l.getPrevisaoRetorno(),
                l.getObservacoes(),
                l.getMedico().getNome(),
                l.getCriadoEm(),
                l.getAtualizadoEm()
        );
    }
}
