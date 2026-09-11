package com.athletepulse.service;

import com.athletepulse.dto.NotificacaoResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.Notificacao;
import com.athletepulse.model.TipoNotificacao;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.NotificacaoRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Geração e consulta de notificações internas do sistema.
 * <p>
 * Os métodos {@code notificar*} são chamados internamente por outros
 * services em resposta a eventos (nova mensagem, treino atribuído, atleta em
 * alerta) - não existe endpoint para criar notificações diretamente, elas
 * são sempre consequência de alguma outra ação no sistema.
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ===== CRIAÇÃO (chamada por outros services) =====

    /** Cria uma notificação para um único destinatário. */
    @Transactional
    public void notificar(Usuario destinatario, TipoNotificacao tipo, String titulo, String mensagem, String link) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setLink(link);

        notificacaoRepository.save(notificacao);
    }

    /**
     * Cria a mesma notificação para todos os usuários de um determinado
     * perfil - usado quando o destinatário não é uma pessoa específica, mas
     * sim "a comissão técnica" ou "a psicologia" como equipe.
     */
    @Transactional
    public void notificarTodosDoTipo(TipoUsuario tipo, TipoNotificacao tipoNotificacao, String titulo, String mensagem, String link) {
        List<Usuario> destinatarios = usuarioRepository.findByTipoOrderByNomeAsc(tipo);
        for (Usuario destinatario : destinatarios) {
            notificar(destinatario, tipoNotificacao, titulo, mensagem, link);
        }
    }

    // ===== CONSULTA (pelo próprio usuário) =====

    /**
     * Lista as notificações mais recentes do usuário autenticado (até 30).
     *
     * @param email e-mail do usuário autenticado
     */
    public List<NotificacaoResponse> listarMinhas(String email) {
        Usuario usuario = buscarPorEmail(email);
        return notificacaoRepository.findTop30ByDestinatario_IdOrderByCriadaEmDesc(usuario.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Conta quantas notificações não lidas o usuário autenticado tem -
     * usado para o número no sininho, consultado periodicamente pelo front.
     *
     * @param email e-mail do usuário autenticado
     */
    public long contarNaoLidas(String email) {
        Usuario usuario = buscarPorEmail(email);
        return notificacaoRepository.countByDestinatario_IdAndLidaFalse(usuario.getId());
    }

    /**
     * Marca uma notificação específica como lida.
     *
     * @param email          e-mail do usuário autenticado
     * @param notificacaoId  identificador da notificação
     * @throws NegocioException se a notificação não existir ou não pertencer ao usuário autenticado (404)
     */
    @Transactional
    public void marcarComoLida(String email, Long notificacaoId) {
        Usuario usuario = buscarPorEmail(email);
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .filter(n -> n.getDestinatario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new NegocioException("Notificação não encontrada.", HttpStatus.NOT_FOUND));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    /**
     * Marca todas as notificações do usuário autenticado como lidas.
     *
     * @param email e-mail do usuário autenticado
     */
    @Transactional
    public void marcarTodasComoLidas(String email) {
        Usuario usuario = buscarPorEmail(email);
        List<Notificacao> naoLidas = notificacaoRepository
                .findTop30ByDestinatario_IdOrderByCriadaEmDesc(usuario.getId())
                .stream()
                .filter(n -> !n.isLida())
                .toList();

        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));
    }

    private NotificacaoResponse paraResponse(Notificacao n) {
        return new NotificacaoResponse(
                n.getId(),
                n.getTipo().name().toLowerCase(),
                n.getTitulo(),
                n.getMensagem(),
                n.getLink(),
                n.isLida(),
                n.getCriadaEm()
        );
    }
}
