package com.athletepulse.service;

import com.athletepulse.dto.AtletaResumoResponse;
import com.athletepulse.dto.CheckInRequest;
import com.athletepulse.dto.CheckInResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CheckIn;
import com.athletepulse.model.TipoNotificacao;
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
    private final NotificacaoService notificacaoService;

    public CheckInService(
            CheckInRepository checkInRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService
    ) {
        this.checkInRepository = checkInRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Registra o check-in diário de um atleta.
     * <p>
     * Se o check-in resultar em status físico "alerta", notifica toda a
     * comissão técnica. Se o estado emocional relatado for muito baixo
     * (1 ou 2), notifica toda a equipe de psicologia.
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

        notificarSeNecessario(atleta, checkIn);

        return paraResponse(checkIn);
    }

    /**
     * Dispara notificações para a comissão técnica (alerta físico) e/ou
     * psicologia (alerta emocional) quando o check-in indicar risco.
     */
    private void notificarSeNecessario(Usuario atleta, CheckIn checkIn) {
        String statusFisico = calcularStatusPontual(checkIn);
        if (statusFisico.equals("alerta")) {
            notificacaoService.notificarTodosDoTipo(
                    TipoUsuario.COMISSAO,
                    TipoNotificacao.ALERTA_ATLETA,
                    atleta.getNome() + " está em alerta",
                    "Dor intensa relatada no check-in de hoje.",
                    "painel-atleta-detalhe.html?id=" + atleta.getId()
            );
        }

        if (checkIn.getEstadoEmocional() <= 2) {
            notificacaoService.notificarTodosDoTipo(
                    TipoUsuario.PSICOLOGO,
                    TipoNotificacao.ALERTA_ATLETA,
                    atleta.getNome() + " está em alerta emocional",
                    "Estado emocional baixo relatado no check-in de hoje.",
                    "painel-psicologo-atleta.html?id=" + atleta.getId()
            );
        }
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
     * Lista o histórico completo de check-ins de um atleta específico, para
     * a comissão técnica visualizar (ex: gráfico de evolução).
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param atletaId      identificador do atleta
     * @throws NegocioException se o usuário não for da comissão (403) ou o id não for de um atleta (404/400)
     */
    public List<CheckInResponse> listarHistoricoDoAtleta(String emailComissao, Long atletaId) {
        Usuario chamador = usuarioRepository.findByEmail(emailComissao)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (chamador.getTipo() != TipoUsuario.COMISSAO) {
            throw new NegocioException("Apenas a comissão técnica pode ver o histórico do atleta.", HttpStatus.FORBIDDEN);
        }

        Usuario atleta = usuarioRepository.findById(atletaId)
                .orElseThrow(() -> new NegocioException("Atleta não encontrado.", HttpStatus.NOT_FOUND));

        if (atleta.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Usuário informado não é um atleta.", HttpStatus.BAD_REQUEST);
        }

        return checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atletaId)
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
                    List<CheckIn> historico = checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atleta.getId());
                    CheckIn ultimo = historico.isEmpty() ? null : historico.get(0);

                    String status = calcularStatus(ultimo);

                    return new AtletaResumoResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            atleta.getEmail(),
                            status,
                            ultimo != null ? paraResponse(ultimo) : null,
                            sugerirIntensidade(status),
                            temRiscoConsecutivo(historico)
                    );
                })
                .toList();
    }

    /**
     * Verifica se os 3 check-ins mais recentes de um atleta (já ordenados do
     * mais novo ao mais antigo) formam uma sequência de 3 dias consecutivos
     * (sem nenhum dia pulado) em que todos classificam como "atenção" ou
     * "alerta" - um padrão persistente, diferente de um dia ruim isolado.
     */
    private boolean temRiscoConsecutivo(List<CheckIn> historicoDescendente) {
        if (historicoDescendente.size() < 3) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            CheckIn atual = historicoDescendente.get(i);

            String statusDoDia = calcularStatusPontual(atual);
            if (!statusDoDia.equals("atencao") && !statusDoDia.equals("alerta")) {
                return false;
            }

            if (i < 2) {
                CheckIn anterior = historicoDescendente.get(i + 1);
                boolean diaSeguido = atual.getDataCheckin().minusDays(1).isEqual(anterior.getDataCheckin());
                if (!diaSeguido) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sugere uma intensidade de treino a partir do status de risco do atleta.
     * É apenas uma sugestão para agilizar a atribuição - a comissão pode
     * escolher qualquer treino do catálogo, independente da sugestão.
     */
    private String sugerirIntensidade(String status) {
        return switch (status) {
            case "alerta" -> "recuperacao";
            case "atencao" -> "leve";
            case "sem_checkin" -> "moderada";
            default -> "intensa";
        };
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

        return calcularStatusPontual(ultimo);
    }

    /**
     * Igual a {@link #calcularStatus}, mas avalia um check-in específico sem
     * exigir que seja de hoje - usado para analisar dias passados na
     * detecção de risco consecutivo ({@link #temRiscoConsecutivo}).
     */
    private String calcularStatusPontual(CheckIn checkIn) {
        boolean dorForte = checkIn.isTemDor() && checkIn.getIntensidadeDor() != null && checkIn.getIntensidadeDor() >= 4;
        if (dorForte) {
            return "alerta";
        }

        boolean precisaAtencao = checkIn.isTemDor() || checkIn.getFadiga() <= 2 || checkIn.getEstadoEmocional() <= 2;
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
