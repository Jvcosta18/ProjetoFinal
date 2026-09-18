package com.athletepulse.controller;

import com.athletepulse.dto.*;
import com.athletepulse.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de perfil próprio e gestão de contas.
 * <p>
 * Rotas sob {@code /me} são de uso de qualquer usuário autenticado, sobre a
 * própria conta. Rotas sob {@code /{id}} são exclusivas da comissão técnica,
 * que acumula o papel de administradora de contas no sistema.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ===== PERFIL PRÓPRIO =====

    /** Retorna os dados do usuário autenticado. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meuPerfil(Authentication auth) {
        return ResponseEntity.ok(usuarioService.meuPerfil(auth.getName()));
    }

    /** Atualiza o nome do usuário autenticado. */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarMeuPerfil(
            @Valid @RequestBody AtualizarPerfilRequest req, Authentication auth
    ) {
        return ResponseEntity.ok(usuarioService.atualizarMeuPerfil(auth.getName(), req));
    }

    /**
     * Troca a senha do usuário autenticado.
     *
     * @return 204 No Content em caso de sucesso; 401 se a senha atual estiver incorreta
     */
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarMinhaSenha(@Valid @RequestBody AlterarSenhaRequest req, Authentication auth) {
        usuarioService.alterarMinhaSenha(auth.getName(), req);
        return ResponseEntity.noContent().build();
    }

    // ===== GESTÃO PELA COMISSÃO TÉCNICA =====

    /** Lista todos os usuários cadastrados no sistema. */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(usuarioService.listarTodos(auth.getName()));
    }

    /** Atualiza nome e status (ativo/desativado) de um usuário. */
    @PutMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long usuarioId, @Valid @RequestBody AdminAtualizarUsuarioRequest req, Authentication auth
    ) {
        return ResponseEntity.ok(usuarioService.atualizarComoComissao(auth.getName(), usuarioId, req));
    }

    /**
     * Redefine a senha de um usuário.
     *
     * @return 204 No Content em caso de sucesso
     */
    @PutMapping("/{usuarioId}/senha")
    public ResponseEntity<Void> redefinirSenha(
            @PathVariable Long usuarioId, @Valid @RequestBody AdminRedefinirSenhaRequest req, Authentication auth
    ) {
        usuarioService.redefinirSenhaComoComissao(auth.getName(), usuarioId, req);
        return ResponseEntity.noContent().build();
    }
}
