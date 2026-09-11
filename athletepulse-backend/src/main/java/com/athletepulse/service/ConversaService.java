package com.athletepulse.service;

import com.athletepulse.dto.ConversaResumoResponse;
import com.athletepulse.dto.MensagemResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.*;
import com.athletepulse.repository.ConversaRepository;
import com.athletepulse.repository.MensagemRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio do sistema de mensagens entre atletas e a equipe
 * (comissão técnica ou psicologia).
 * <p>
 * Cada atleta possui, no máximo, uma {@link Conversa} por {@link CanalConversa}.
 * A conversa é criada automaticamente ("lazy") na primeira mensagem enviada
 * por qualquer um dos lados - não existe um endpoint explícito de "criar conversa".
 * O canal do lado da equipe é sempre determinado pelo {@link TipoUsuario} de
 * quem está autenticado, nunca escolhido livremente.
 */
@Service
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public ConversaService(
            ConversaRepository conversaRepository,
            MensagemRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService
    ) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    // ===== LADO DO ATLETA =====

    /**
     * Lista as mensagens da conversa do atleta autenticado com um canal específico.
     *
     * @param emailAtleta e-mail do usuário autenticado (deve ser atleta)
     * @param canal       canal da conversa (comissão ou psicologia)
     * @throws NegocioException se o usuário não for atleta (403)
     */
    public List<MensagemResponse> listarMinhasMensagens(String emailAtleta, CanalConversa canal) {
        Usuario atleta = buscarUsuarioPorTipo(emailAtleta, TipoUsuario.JOGADOR);
        Conversa conversa = obterOuCriarConversa(atleta, canal);
        return montarMensagens(conversa.getId(), atleta.getId());
    }

    /**
     * Envia uma mensagem do atleta autenticado para um canal específico.
     *
     * @param emailAtleta e-mail do usuário autenticado (deve ser atleta)
     * @param canal       canal de destino (comissão ou psicologia)
     * @param texto       conteúdo da mensagem
     * @return a mensagem criada
     * @throws NegocioException se o usuário não for atleta (403)
     */
    @Transactional
    public MensagemResponse enviarComoAtleta(String emailAtleta, CanalConversa canal, String texto) {
        Usuario atleta = buscarUsuarioPorTipo(emailAtleta, TipoUsuario.JOGADOR);
        Conversa conversa = obterOuCriarConversa(atleta, canal);
        MensagemResponse resposta = salvarMensagem(conversa, atleta, texto, atleta.getId());

        TipoUsuario tipoEquipe = canal == CanalConversa.COMISSAO ? TipoUsuario.COMISSAO : TipoUsuario.PSICOLOGO;
        notificacaoService.notificarTodosDoTipo(
                tipoEquipe,
                TipoNotificacao.MENSAGEM,
                "Nova mensagem de " + atleta.getNome(),
                resumirTexto(texto),
                "painel-conversa.html?atletaId=" + atleta.getId()
        );

        return resposta;
    }

    // ===== LADO DA EQUIPE (comissão ou psicólogo) =====

    /**
     * Lista, para a equipe autenticada (comissão ou psicologia), um resumo
     * da conversa com cada atleta - usado como caixa de entrada.
     *
     * @param emailStaff e-mail do usuário autenticado (deve ser comissão ou psicólogo)
     * @throws NegocioException se o usuário não for da comissão nem psicólogo (403)
     */
    public List<ConversaResumoResponse> listarConversasDaEquipe(String emailStaff) {
        Usuario staff = buscarUsuario(emailStaff);
        CanalConversa canal = canalDoTipo(staff.getTipo());

        List<Usuario> atletas = usuarioRepository.findByTipoOrderByNomeAsc(TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> {
                    Conversa conversa = conversaRepository.findByAtleta_IdAndCanal(atleta.getId(), canal).orElse(null);

                    if (conversa == null) {
                        return new ConversaResumoResponse(atleta.getId(), atleta.getNome(), null, null, false, true);
                    }

                    Mensagem ultima = mensagemRepository
                            .findFirstByConversa_IdOrderByEnviadaEmDesc(conversa.getId())
                            .orElse(null);

                    if (ultima == null) {
                        return new ConversaResumoResponse(atleta.getId(), atleta.getNome(), null, null, false, true);
                    }

                    return new ConversaResumoResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            ultima.getTexto(),
                            ultima.getEnviadaEm(),
                            ultima.getAutor().getId().equals(atleta.getId()),
                            false
                    );
                })
                .toList();
    }

    /**
     * Lista as mensagens da conversa entre a equipe autenticada e um atleta específico.
     *
     * @param emailStaff e-mail do usuário autenticado (deve ser comissão ou psicólogo)
     * @param atletaId   identificador do atleta
     * @throws NegocioException se o usuário não for da equipe (403) ou o id não for de um atleta (404/400)
     */
    public List<MensagemResponse> listarMensagensComAtleta(String emailStaff, Long atletaId) {
        Usuario staff = buscarUsuario(emailStaff);
        CanalConversa canal = canalDoTipo(staff.getTipo());
        Usuario atleta = buscarAtletaPorId(atletaId);

        Conversa conversa = obterOuCriarConversa(atleta, canal);
        return montarMensagens(conversa.getId(), staff.getId());
    }

    /**
     * Envia uma mensagem da equipe autenticada para um atleta específico.
     *
     * @param emailStaff e-mail do usuário autenticado (deve ser comissão ou psicólogo)
     * @param atletaId   identificador do atleta destinatário
     * @param texto      conteúdo da mensagem
     * @return a mensagem criada
     * @throws NegocioException se o usuário não for da equipe (403) ou o id não for de um atleta (404/400)
     */
    @Transactional
    public MensagemResponse enviarComoStaff(String emailStaff, Long atletaId, String texto) {
        Usuario staff = buscarUsuario(emailStaff);
        CanalConversa canal = canalDoTipo(staff.getTipo());
        Usuario atleta = buscarAtletaPorId(atletaId);

        Conversa conversa = obterOuCriarConversa(atleta, canal);
        MensagemResponse resposta = salvarMensagem(conversa, staff, texto, staff.getId());

        String nomeCanal = canal == CanalConversa.COMISSAO ? "Comissão Técnica" : "Psicologia";
        notificacaoService.notificar(
                atleta,
                TipoNotificacao.MENSAGEM,
                "Nova mensagem da " + nomeCanal,
                resumirTexto(texto),
                "painel-mensagens.html"
        );

        return resposta;
    }

    // ===== INTERNO =====

    /** Busca a conversa existente de um atleta com um canal, ou cria uma nova se ainda não existir. */
    private Conversa obterOuCriarConversa(Usuario atleta, CanalConversa canal) {
        return conversaRepository.findByAtleta_IdAndCanal(atleta.getId(), canal)
                .orElseGet(() -> {
                    Conversa nova = new Conversa();
                    nova.setAtleta(atleta);
                    nova.setCanal(canal);
                    return conversaRepository.save(nova);
                });
    }

    /** Persiste uma nova mensagem numa conversa e converte para o DTO de resposta. */
    private MensagemResponse salvarMensagem(Conversa conversa, Usuario autor, String texto, Long idViewer) {
        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setAutor(autor);
        mensagem.setTexto(texto.trim());

        mensagemRepository.save(mensagem);
        return paraResponse(mensagem, idViewer);
    }

    /** Lista as mensagens de uma conversa em ordem cronológica, já convertidas para DTO. */
    private List<MensagemResponse> montarMensagens(Long conversaId, Long idViewer) {
        return mensagemRepository.findByConversa_IdOrderByEnviadaEmAsc(conversaId)
                .stream()
                .map(m -> paraResponse(m, idViewer))
                .toList();
    }

    /**
     * Converte a entidade {@link Mensagem} no DTO de resposta, calculando o
     * campo {@code minhaMensagem} relativo a quem está consultando a conversa.
     */
    private MensagemResponse paraResponse(Mensagem m, Long idViewer) {
        return new MensagemResponse(
                m.getId(),
                m.getTexto(),
                m.getAutor().getNome(),
                m.getAutor().getTipo().name(),
                m.getAutor().getId().equals(idViewer),
                m.getEnviadaEm()
        );
    }

    /** Corta o texto da mensagem para caber no campo de descrição da notificação. */
    private String resumirTexto(String texto) {
        String limpo = texto.trim();
        return limpo.length() > 120 ? limpo.substring(0, 117) + "..." : limpo;
    }

    /** Determina o canal correspondente ao perfil de um membro da equipe. */
    private CanalConversa canalDoTipo(TipoUsuario tipo) {
        if (tipo == TipoUsuario.COMISSAO) return CanalConversa.COMISSAO;
        if (tipo == TipoUsuario.PSICOLOGO) return CanalConversa.PSICOLOGO;
        throw new NegocioException("Apenas comissão técnica ou psicologia podem acessar essa área.", HttpStatus.FORBIDDEN);
    }

    /** Busca um usuário pelo e-mail, sem restrição de perfil. */
    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));
    }

    /** Busca um usuário pelo e-mail e garante que seu perfil é o esperado. */
    private Usuario buscarUsuarioPorTipo(String email, TipoUsuario tipoEsperado) {
        Usuario usuario = buscarUsuario(email);
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
}
