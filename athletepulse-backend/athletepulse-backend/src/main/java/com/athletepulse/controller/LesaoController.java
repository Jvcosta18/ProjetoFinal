package com.athletepulse.controller;

import com.athletepulse.dto.AtletaMedicoResponse;
import com.athletepulse.dto.LesaoRequest;
import com.athletepulse.dto.LesaoResponse;
import com.athletepulse.service.LesaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints do departamento médico/fisioterapia: visão do elenco por
 * liberação e registro de lesões.
 */
@RestController
@RequestMapping("/api/medico")
public class LesaoController {

    private final LesaoService lesaoService;

    public LesaoController(LesaoService lesaoService) {
        this.lesaoService = lesaoService;
    }

    /** Lista todos os atletas com o status de liberação atual de cada um. */
    @GetMapping("/atletas")
    public ResponseEntity<List<AtletaMedicoResponse>> listarAtletas(Authentication auth) {
        return ResponseEntity.ok(lesaoService.listarAtletas(auth.getName()));
    }

    /** Lista o histórico de lesões de um atleta específico. */
    @GetMapping("/atletas/{atletaId}/lesoes")
    public ResponseEntity<List<LesaoResponse>> listarHistorico(@PathVariable Long atletaId, Authentication auth) {
        return ResponseEntity.ok(lesaoService.listarHistorico(auth.getName(), atletaId));
    }

    /**
     * Registra uma nova lesão para um atleta.
     *
     * @return 201 Created com a lesão criada
     */
    @PostMapping("/atletas/{atletaId}/lesoes")
    public ResponseEntity<LesaoResponse> registrar(
            @PathVariable Long atletaId, @Valid @RequestBody LesaoRequest req, Authentication auth
    ) {
        LesaoResponse resposta = lesaoService.registrar(auth.getName(), atletaId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /** Atualiza uma lesão existente (ex: evoluir o status de recuperação). */
    @PutMapping("/lesoes/{lesaoId}")
    public ResponseEntity<LesaoResponse> atualizar(
            @PathVariable Long lesaoId, @Valid @RequestBody LesaoRequest req, Authentication auth
    ) {
        return ResponseEntity.ok(lesaoService.atualizar(auth.getName(), lesaoId, req));
    }
}
