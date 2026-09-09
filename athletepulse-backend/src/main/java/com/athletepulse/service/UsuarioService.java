package com.athletepulse.service;

import com.athletepulse.dto.*;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio de gestão de conta: perfil próprio (qualquer usuário) e
 * administração de contas (exclusiva da comissão técnica).
 * <p>
 * O sistema não possui um perfil de "administrador" separado - a comissão
 * técnica acumula esse papel, já que é ela quem gerencia o elenco no dia a dia.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== PERFIL PRÓPRIO =====

    /**
     * Retorna os dados do usuário autenticado.
     *
     * @param email e-mail do usuário autenticado
     */
    public UsuarioResponse meuPerfil(String email) {
        return paraResponse(buscarPorEmail(email));
    }

    /**
     * Atualiza o nome do usuário autenticado.
     *
     * @param email e-mail do usuário autenticado
     * @param req   novo nome
     * @return dados atualizados
     */
    @Transactional
    public UsuarioResponse atualizarMeuPerfil(String email, AtualizarPerfilRequest req) {
        Usuario usuario = buscarPorEmail(email);
        usuario.setNome(req.nome().trim());
        usuarioRepository.save(usuario);
        return paraResponse(usuario);
    }

    /**
     * Troca a senha do usuário autenticado, exigindo a senha atual como confirmação.
     *
     * @param email e-mail do usuário autenticado
     * @param req   senha atual e nova senha
     * @throws NegocioException se a senha atual informada estiver incorreta (401)
     */
    @Transactional
    public void alterarMinhaSenha(String email, AlterarSenhaRequest req) {
        Usuario usuario = buscarPorEmail(email);

        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenha())) {
            throw new NegocioException("Senha atual incorreta.", HttpStatus.UNAUTHORIZED);
        }

        usuario.setSenha(passwordEncoder.encode(req.novaSenha()));
        usuarioRepository.save(usuario);
    }

    // ===== GESTÃO PELA COMISSÃO TÉCNICA =====

    /**
     * Lista todos os usuários cadastrados no sistema, de qualquer perfil.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @throws NegocioException se o usuário não for da comissão técnica (403)
     */
    public List<UsuarioResponse> listarTodos(String emailComissao) {
        exigirComissao(emailComissao);
        return usuarioRepository.findAll()
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Atualiza nome e status (ativo/desativado) de um usuário.
     * <p>
     * Uma conta desativada não consegue mais fazer login nem usar um token
     * já emitido (ver {@link com.athletepulse.security.UsuarioDetailsService}
     * e {@link com.athletepulse.security.JwtAuthFilter}), mas todo o
     * histórico dela é preservado.
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param usuarioId     identificador do usuário a editar
     * @param req           novos dados
     * @return dados atualizados
     * @throws NegocioException se quem chama não for da comissão (403), o
     *                          usuário-alvo não existir (404), ou for uma
     *                          tentativa de a comissão desativar a própria conta (400)
     */
    @Transactional
    public UsuarioResponse atualizarComoComissao(String emailComissao, Long usuarioId, AdminAtualizarUsuarioRequest req) {
        Usuario comissao = exigirComissao(emailComissao);
        Usuario alvo = buscarPorId(usuarioId);

        if (comissao.getId().equals(alvo.getId()) && !req.ativo()) {
            throw new NegocioException("Você não pode desativar a própria conta.", HttpStatus.BAD_REQUEST);
        }

        alvo.setNome(req.nome().trim());
        alvo.setAtivo(req.ativo());
        usuarioRepository.save(alvo);
        return paraResponse(alvo);
    }

    /**
     * Redefine a senha de um usuário (substitui a recuperação por e-mail).
     *
     * @param emailComissao e-mail do usuário autenticado (deve ser da comissão técnica)
     * @param usuarioId     identificador do usuário-alvo
     * @param req           nova senha
     * @throws NegocioException se quem chama não for da comissão (403) ou o usuário-alvo não existir (404)
     */
    @Transactional
    public void redefinirSenhaComoComissao(String emailComissao, Long usuarioId, AdminRedefinirSenhaRequest req) {
        exigirComissao(emailComissao);
        Usuario alvo = buscarPorId(usuarioId);

        alvo.setSenha(passwordEncoder.encode(req.novaSenha()));
        usuarioRepository.save(alvo);
    }

    /** Busca o usuário pelo e-mail e garante que seu perfil é {@link TipoUsuario#COMISSAO}. */
    private Usuario exigirComissao(String email) {
        Usuario usuario = buscarPorEmail(email);
        if (usuario.getTipo() != TipoUsuario.COMISSAO) {
            throw new NegocioException("Apenas a comissão técnica pode acessar essa área.", HttpStatus.FORBIDDEN);
        }
        return usuario;
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.NOT_FOUND));
    }

    private UsuarioResponse paraResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getTipo().name().toLowerCase(),
                u.isAtivo(),
                u.getCriadoEm()
        );
    }
}
