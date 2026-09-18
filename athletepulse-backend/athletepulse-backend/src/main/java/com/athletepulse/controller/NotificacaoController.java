package com.athletepulse.controller;

import com.athletepulse.dto.ContagemNaoLidasResponse;
import com.athletepulse.dto.NotificacaoResponse;
import com.athletepulse.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de notificações internas, disponíveis para qualquer usuário
 * autenticado sobre as próprias notificações.
 */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    /** Lista as notificações mais recentes do usuário autenticado. */
    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> listarMinhas(Authentication auth) {
        return ResponseEntity.ok(notificacaoService.listarMinhas(auth.getName()));
    }

    /** Retorna a contagem de notificações não lidas - consultado periodicamente pelo front para o sininho. */
    @GetMapping("/nao-lidas/contagem")
    public ResponseEntity<ContagemNaoLidasResponse> contarNaoLidas(Authentication auth) {
        return ResponseEntity.ok(new ContagemNaoLidasResponse(notificacaoService.contarNaoLidas(auth.getName())));
    }

    /** Marca uma notificação específica como lida. */
    @PutMapping("/{notificacaoId}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long notificacaoId, Authentication auth) {
        notificacaoService.marcarComoLida(auth.getName(), notificacaoId);
        return ResponseEntity.noContent().build();
    }

    /** Marca todas as notificações do usuário autenticado como lidas. */
    @PutMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Authentication auth) {
        notificacaoService.marcarTodasComoLidas(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
