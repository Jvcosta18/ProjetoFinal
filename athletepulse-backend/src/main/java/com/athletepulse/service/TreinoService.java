package com.athletepulse.service;

import com.athletepulse.dto.AtribuirTreinoRequest;
import com.athletepulse.dto.TreinoAtribuidoResponse;
import com.athletepulse.dto.TreinoRequest;
import com.athletepulse.dto.TreinoResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.*;
import com.athletepulse.repository.TreinoAtribuidoRepository;
import com.athletepulse.repository.TreinoRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio do catálogo de treinos e da atribuição diária aos atletas.
 * <p>
 * O catálogo ({@link Treino}) é reutilizável: a comissão cadastra um treino
 * uma vez e o atribui a quantos atletas e dias quiser. Já a atribuição
 * ({@link TreinoAtribuido}) é diária - existe no máximo uma por atleta por
 * dia, mas pode ser sobrescrita pela comissão a qualquer momento naquele
 * mesmo dia (ex: o atleta piorou e o plano precisa mudar).
 */
@Service
public class TreinoService {

    private final TreinoRepository treinoRepository;
    private final TreinoAtribuidoRepository treinoAtribuidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public TreinoService(
            TreinoRepository treinoRepository,
            TreinoAtribuidoRepository treinoAtribuidoRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService
    ) {
        this.treinoRepository = treinoRepository;
        this.treinoAtribuidoRepository = treinoAtribuidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Cadastra um novo treino no catálogo.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param req           dados do treino
     * @return o treino criado
     * @throws NegocioException se o usuário não for da comissão (403) ou a intensidade for inválida (400)
     */
    @Transactional
    public TreinoResponse criarTreino(String emailComissao, TreinoRequest req) {
        Usuario comissao = exigirComissao(emailComissao);

        Treino treino = new Treino();
        treino.setTitulo(req.titulo().trim());
        treino.setDescricao(req.descricao().trim());
        treino.setIntensidade(converterIntensidade(req.intensidade()));
        treino.setCriadoPor(comissao);

        treinoRepository.save(treino);
        return paraResponse(treino);
    }

    /**
     * Lista todo o catálogo de treinos, do mais recente ao mais antigo.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @throws NegocioException se o usuário não for da comissão (403)
     */
    public List<TreinoResponse> listarCatalogo(String emailComissao) {
        exigirComissao(emailComissao);
        return treinoRepository.findAllByOrderByCriadoEmDesc()
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Atribui um treino do catálogo a um atleta, para hoje.
     * <p>
     * Se o atleta já tiver um treino atribuído hoje, ele é substituído pelo
     * novo (não gera erro de duplicidade) - permite à comissão corrigir o
     * plano do dia a qualquer momento.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param req           atleta, treino e observações
     * @return a atribuição criada ou atualizada
     * @throws NegocioException se o usuário não for da comissão (403), ou
     *                          o atleta/treino informado não existir (404)
     */
    @Transactional
    public TreinoAtribuidoResponse atribuir(String emailComissao, AtribuirTreinoRequest req) {
        Usuario comissao = exigirComissao(emailComissao);
        Usuario atleta = buscarAtletaPorId(req.atletaId());
        Treino treino = buscarTreinoPorId(req.treinoId());

        TreinoAtribuido atribuicao = treinoAtribuidoRepository
                .findByAtleta_IdAndData(atleta.getId(), LocalDate.now())
                .orElseGet(TreinoAtribuido::new);

        atribuicao.setAtleta(atleta);
        atribuicao.setTreino(treino);
        atribuicao.setAtribuidoPor(comissao);
        atribuicao.setData(LocalDate.now());
        atribuicao.setObservacoes(req.observacoes());
        atribuicao.setAtualizadoEm(java.time.LocalDateTime.now());

        treinoAtribuidoRepository.save(atribuicao);

        notificacaoService.notificar(
                atleta,
                TipoNotificacao.TREINO,
                "Novo treino atribuído",
                treino.getTitulo(),
                "painel-jogador.html"
        );

        return paraResponseAtribuicao(atribuicao);
    }

    /**
     * Lista, para cada atleta, o treino atribuído hoje (ou {@code null} se ainda não houver um).
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @throws NegocioException se o usuário não for da comissão (403)
     */
    public List<TreinoAtribuidoResponse> listarAtribuicoesDeHoje(String emailComissao) {
        exigirComissao(emailComissao);

        List<Usuario> atletas = usuarioRepository.findByTipoOrderByNomeAsc(TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> treinoAtribuidoRepository
                        .findByAtleta_IdAndData(atleta.getId(), LocalDate.now())
                        .map(this::paraResponseAtribuicao)
                        .orElse(new TreinoAtribuidoResponse(null, atleta.getId(), atleta.getNome(), null, LocalDate.now(), null, null))
                )
                .toList();
    }

    /**
     * Retorna o treino atribuído hoje ao atleta autenticado, se houver.
     *
     * @param emailAtleta e-mail do usuário autenticado (deve ser atleta)
     * @throws NegocioException se o usuário não for atleta (403)
     */
    public Optional<TreinoAtribuidoResponse> meuTreinoDeHoje(String emailAtleta) {
        Usuario atleta = buscarUsuarioPorTipo(emailAtleta, TipoUsuario.JOGADOR);
        return treinoAtribuidoRepository
                .findByAtleta_IdAndData(atleta.getId(), LocalDate.now())
                .map(this::paraResponseAtribuicao);
    }

    /**
     * Lista o histórico de treinos atribuídos ao atleta autenticado, do mais recente ao mais antigo.
     *
     * @param emailAtleta e-mail do usuário autenticado (deve ser atleta)
     * @throws NegocioException se o usuário não for atleta (403)
     */
    public List<TreinoAtribuidoResponse> meuHistorico(String emailAtleta) {
        Usuario atleta = buscarUsuarioPorTipo(emailAtleta, TipoUsuario.JOGADOR);
        return treinoAtribuidoRepository.findByAtleta_IdOrderByDataDesc(atleta.getId())
                .stream()
                .map(this::paraResponseAtribuicao)
                .toList();
    }

    /**
     * Edita um treino existente do catálogo.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param treinoId      identificador do treino a editar
     * @param req           novos dados do treino
     * @return o treino atualizado
     * @throws NegocioException se o usuário não for da comissão (403), o
     *                          treino não existir (404), ou a intensidade for inválida (400)
     */
    @Transactional
    public TreinoResponse editarTreino(String emailComissao, Long treinoId, TreinoRequest req) {
        exigirComissao(emailComissao);
        Treino treino = buscarTreinoPorId(treinoId);

        treino.setTitulo(req.titulo().trim());
        treino.setDescricao(req.descricao().trim());
        treino.setIntensidade(converterIntensidade(req.intensidade()));

        treinoRepository.save(treino);
        return paraResponse(treino);
    }

    /**
     * Remove um treino do catálogo.
     * <p>
     * Por integridade do histórico, não é possível excluir um treino que já
     * tenha sido atribuído a algum atleta em algum dia - nesse caso, a
     * comissão deve apenas editá-lo em vez de removê-lo.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param treinoId      identificador do treino a remover
     * @throws NegocioException se o usuário não for da comissão (403), o
     *                          treino não existir (404), ou o treino já
     *                          tiver sido atribuído a algum atleta (409)
     */
    @Transactional
    public void excluirTreino(String emailComissao, Long treinoId) {
        exigirComissao(emailComissao);
        Treino treino = buscarTreinoPorId(treinoId);

        if (treinoAtribuidoRepository.existsByTreino_Id(treinoId)) {
            throw new NegocioException(
                    "Este treino já foi atribuído a algum atleta e não pode ser excluído. Edite-o em vez de remover.",
                    HttpStatus.CONFLICT
            );
        }

        treinoRepository.delete(treino);
    }

    /** Busca um treino do catálogo pelo id. */
    private Treino buscarTreinoPorId(Long id) {
        return treinoRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Treino não encontrado.", HttpStatus.NOT_FOUND));
    }

    /** Converte a string de intensidade (ex: "leve") para o enum {@link IntensidadeTreino}. */
    private IntensidadeTreino converterIntensidade(String valor) {
        try {
            return IntensidadeTreino.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Intensidade de treino inválida.", HttpStatus.BAD_REQUEST);
        }
    }

    /** Busca o usuário pelo e-mail e garante que seu perfil é {@link TipoUsuario#COMISSAO}. */
    private Usuario exigirComissao(String email) {
        return buscarUsuarioPorTipo(email, TipoUsuario.COMISSAO);
    }

    /** Busca o usuário pelo e-mail e garante que seu perfil é o esperado. */
    private Usuario buscarUsuarioPorTipo(String email, TipoUsuario tipoEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != tipoEsperado) {
            throw new NegocioException("Acesso não permitido para esse perfil.", HttpStatus.FORBIDDEN);
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

    /** Converte a entidade {@link Treino} no DTO de resposta. */
    private TreinoResponse paraResponse(Treino t) {
        return new TreinoResponse(
                t.getId(),
                t.getTitulo(),
                t.getDescricao(),
                t.getIntensidade().name().toLowerCase(),
                t.getCriadoEm()
        );
    }

    /** Converte a entidade {@link TreinoAtribuido} no DTO de resposta. */
    private TreinoAtribuidoResponse paraResponseAtribuicao(TreinoAtribuido a) {
        return new TreinoAtribuidoResponse(
                a.getId(),
                a.getAtleta().getId(),
                a.getAtleta().getNome(),
                paraResponse(a.getTreino()),
                a.getData(),
                a.getObservacoes(),
                a.getAtualizadoEm()
        );
    }
}
