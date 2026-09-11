package com.athletepulse.service;

import com.athletepulse.dto.AtletaEmocionalResponse;
import com.athletepulse.dto.NotaRequest;
import com.athletepulse.dto.NotaResponse;
import com.athletepulse.dto.PontoEmocionalResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CheckIn;
import com.athletepulse.model.NotaPsicologica;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.CheckInRepository;
import com.athletepulse.repository.NotaPsicologicaRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio da área da psicologia: acompanhamento emocional dos
 * atletas e notas privadas.
 * <p>
 * Todo o conteúdo aqui é isolado dos demais perfis - a comissão técnica não
 * tem acesso a nenhum endpoint deste service.
 */
@Service
public class PsicologoService {

    private final UsuarioRepository usuarioRepository;
    private final CheckInRepository checkInRepository;
    private final NotaPsicologicaRepository notaRepository;

    public PsicologoService(
            UsuarioRepository usuarioRepository,
            CheckInRepository checkInRepository,
            NotaPsicologicaRepository notaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.checkInRepository = checkInRepository;
        this.notaRepository = notaRepository;
    }

    /**
     * Lista todos os atletas com um resumo do estado emocional do dia,
     * calculado a partir do check-in mais recente. Dados físicos (dor,
     * fadiga) não são incluídos aqui.
     *
     * @param emailPsicologo e-mail do usuário autenticado (deve ser psicólogo)
     * @throws NegocioException se o usuário não for psicólogo (403)
     */
    public List<AtletaEmocionalResponse> listarAtletas(String emailPsicologo) {
        exigirPsicologo(emailPsicologo);

        List<Usuario> atletas = usuarioRepository.findByTipoOrderByNomeAsc(TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> {
                    List<CheckIn> historico = checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atleta.getId());
                    CheckIn ultimo = historico.isEmpty() ? null : historico.get(0);

                    return new AtletaEmocionalResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            atleta.getEmail(),
                            calcularStatusEmocional(ultimo),
                            ultimo != null ? ultimo.getEstadoEmocional() : null,
                            ultimo != null ? ultimo.getDataCheckin().toString() : null,
                            temQuedaConsecutiva(historico)
                    );
                })
                .toList();
    }

    /**
     * Lista o histórico emocional de um atleta (apenas data e estado
     * emocional de cada dia), para o gráfico de evolução no painel do psicólogo.
     *
     * @param emailPsicologo e-mail do usuário autenticado (deve ser psicólogo)
     * @param atletaId       identificador do atleta
     * @throws NegocioException se o usuário não for psicólogo (403) ou o id não for de um atleta (404/400)
     */
    public List<PontoEmocionalResponse> listarHistoricoEmocional(String emailPsicologo, Long atletaId) {
        exigirPsicologo(emailPsicologo);
        buscarAtletaPorId(atletaId);

        return checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atletaId)
                .stream()
                .map(c -> new PontoEmocionalResponse(c.getDataCheckin(), c.getEstadoEmocional()))
                .toList();
    }

    /**
     * Lista o histórico de notas de um atleta, da mais recente à mais antiga.
     * <p>
     * O histórico é compartilhado entre todos os psicólogos da equipe (não
     * filtrado por autor), permitindo continuidade no acompanhamento mesmo
     * que outro profissional tenha escrito a nota.
     *
     * @param emailPsicologo e-mail do usuário autenticado (deve ser psicólogo)
     * @param atletaId       identificador do atleta
     * @throws NegocioException se o usuário não for psicólogo (403) ou o id não for de um atleta (404/400)
     */
    public List<NotaResponse> listarNotas(String emailPsicologo, Long atletaId) {
        exigirPsicologo(emailPsicologo);
        buscarAtletaPorId(atletaId);

        return notaRepository.findByAtleta_IdOrderByCriadoEmDesc(atletaId)
                .stream()
                .map(n -> new NotaResponse(n.getId(), n.getTexto(), n.getPsicologo().getNome(), n.getCriadoEm()))
                .toList();
    }

    /**
     * Cria uma nova nota de acompanhamento para um atleta.
     *
     * @param emailPsicologo e-mail do usuário autenticado (deve ser psicólogo)
     * @param atletaId       identificador do atleta
     * @param req            conteúdo da nota
     * @return a nota criada
     * @throws NegocioException se o usuário não for psicólogo (403) ou o id não for de um atleta (404/400)
     */
    @Transactional
    public NotaResponse criarNota(String emailPsicologo, Long atletaId, NotaRequest req) {
        Usuario psicologo = exigirPsicologo(emailPsicologo);
        Usuario atleta = buscarAtletaPorId(atletaId);

        NotaPsicologica nota = new NotaPsicologica();
        nota.setAtleta(atleta);
        nota.setPsicologo(psicologo);
        nota.setTexto(req.texto().trim());

        notaRepository.save(nota);
        return new NotaResponse(nota.getId(), nota.getTexto(), psicologo.getNome(), nota.getCriadoEm());
    }

    /**
     * Calcula a classificação emocional de um atleta a partir do check-in de hoje:
     * <ul>
     *   <li>{@code sem_dado} - não enviou check-in hoje</li>
     *   <li>{@code alerta} - estado emocional 1 ou 2</li>
     *   <li>{@code atencao} - estado emocional 3</li>
     *   <li>{@code ok} - estado emocional 4 ou 5</li>
     * </ul>
     */
    private String calcularStatusEmocional(CheckIn ultimo) {
        if (ultimo == null || !ultimo.getDataCheckin().isEqual(LocalDate.now())) {
            return "sem_dado";
        }

        return calcularStatusEmocionalPontual(ultimo);
    }

    /**
     * Igual a {@link #calcularStatusEmocional}, mas avalia um check-in
     * específico sem exigir que seja de hoje - usado para analisar dias
     * passados na detecção de queda emocional consecutiva.
     */
    private String calcularStatusEmocionalPontual(CheckIn checkIn) {
        int estado = checkIn.getEstadoEmocional();
        if (estado <= 2) return "alerta";
        if (estado == 3) return "atencao";
        return "ok";
    }

    /**
     * Verifica se os 3 check-ins mais recentes de um atleta (já ordenados do
     * mais novo ao mais antigo) formam uma sequência de 3 dias consecutivos
     * em que o estado emocional foi "atenção" ou "alerta" em todos eles.
     */
    private boolean temQuedaConsecutiva(List<CheckIn> historicoDescendente) {
        if (historicoDescendente.size() < 3) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            CheckIn atual = historicoDescendente.get(i);

            String statusDoDia = calcularStatusEmocionalPontual(atual);
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

    /** Busca o usuário pelo e-mail e garante que seu perfil é {@link TipoUsuario#PSICOLOGO}. */
    private Usuario exigirPsicologo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != TipoUsuario.PSICOLOGO) {
            throw new NegocioException("Apenas a psicologia pode acessar essa área.", HttpStatus.FORBIDDEN);
        }

        return usuario;
    }

    /** Busca um usuário pelo id e garante que seu perfil é {@link TipoUsuario#JOGADOR}. */
    private Usuario buscarAtletaPorId(Long id) {
        Usuario atleta = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Atleta não encontrado.", HttpStatus.NOT_FOUND));

        if (atleta.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Usuário informado não é um atleta.", HttpStatus.BAD_REQUEST);
        }

        return atleta;
    }
}
