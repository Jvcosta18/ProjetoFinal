package com.athletepulse.controller;

import com.athletepulse.dto.ConversaResumoResponse;
import com.athletepulse.dto.MensagemRequest;
import com.athletepulse.dto.MensagemResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CanalConversa;
import com.athletepulse.service.ConversaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints do sistema de mensagens entre atletas e a equipe (comissão ou psicologia).
 * <p>
 * Duas famílias de rotas: {@code /meu/{canal}} usada pelo atleta (que escolhe
 * o canal de destino) e {@code /conversas} / {@code /atleta/{id}} usadas pela
 * equipe (cujo canal é sempre implícito, determinado pelo próprio perfil).
 */
@RestController
@RequestMapping("/api/mensagens")
public class ConversaController {

    private final ConversaService conversaService;

    public ConversaController(ConversaService conversaService) {
        this.conversaService = conversaService;
    }

    // ===== ATLETA fala com um canal (comissao | psicologo) =====

    /**
     * Lista as mensagens da conversa do atleta autenticado com um canal.
     *
     * @param canal "comissao" ou "psicologo"
     */
    @GetMapping("/meu/{canal}")
    public ResponseEntity<List<MensagemResponse>> listarMinhasMensagens(
            @PathVariable String canal, Authentication auth
    ) {
        return ResponseEntity.ok(conversaService.listarMinhasMensagens(auth.getName(), converterCanal(canal)));
    }

    /**
     * Envia uma mensagem do atleta autenticado para um canal.
     *
     * @param canal "comissao" ou "psicologo"
     * @return 201 Created com a mensagem criada
     */
    @PostMapping("/meu/{canal}")
    public ResponseEntity<MensagemResponse> enviarComoAtleta(
            @PathVariable String canal, @Valid @RequestBody MensagemRequest req, Authentication auth
    ) {
        MensagemResponse resposta = conversaService.enviarComoAtleta(auth.getName(), converterCanal(canal), req.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // ===== EQUIPE (comissão ou psicólogo) fala com um atleta =====

    /** Lista, para a equipe autenticada, a caixa de entrada com um resumo da conversa de cada atleta. */
    @GetMapping("/conversas")
    public ResponseEntity<List<ConversaResumoResponse>> listarConversas(Authentication auth) {
        return ResponseEntity.ok(conversaService.listarConversasDaEquipe(auth.getName()));
    }

    /** Lista as mensagens da conversa entre a equipe autenticada e um atleta específico. */
    @GetMapping("/atleta/{atletaId}")
    public ResponseEntity<List<MensagemResponse>> listarMensagensComAtleta(
            @PathVariable Long atletaId, Authentication auth
    ) {
        return ResponseEntity.ok(conversaService.listarMensagensComAtleta(auth.getName(), atletaId));
    }

    /**
     * Envia uma mensagem da equipe autenticada para um atleta específico.
     *
     * @return 201 Created com a mensagem criada
     */
    @PostMapping("/atleta/{atletaId}")
    public ResponseEntity<MensagemResponse> enviarComoStaff(
            @PathVariable Long atletaId, @Valid @RequestBody MensagemRequest req, Authentication auth
    ) {
        MensagemResponse resposta = conversaService.enviarComoStaff(auth.getName(), atletaId, req.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /** Converte a string do canal (ex: "comissao") para o enum {@link CanalConversa}, com erro amigável se inválida. */
    private CanalConversa converterCanal(String valor) {
        try {
            return CanalConversa.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Canal inválido.", HttpStatus.BAD_REQUEST);
        }
    }
}
