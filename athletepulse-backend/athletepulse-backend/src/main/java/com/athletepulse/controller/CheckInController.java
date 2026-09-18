package com.athletepulse.controller;

import com.athletepulse.dto.AtletaResumoResponse;
import com.athletepulse.dto.CheckInRequest;
import com.athletepulse.dto.CheckInResponse;
import com.athletepulse.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de check-in diário: envio pelo atleta e visão de elenco para a comissão técnica.
 * <p>
 * Todas as rotas exigem autenticação JWT. O e-mail do usuário autenticado é
 * obtido via {@code Authentication.getName()}, preenchido pelo
 * {@link com.athletepulse.security.JwtAuthFilter}.
 */
@RestController
@RequestMapping("/api/checkins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * Registra o check-in diário do atleta autenticado.
     *
     * @return 201 Created com o check-in criado; 403 se não for atleta; 409 se já enviou hoje
     */
    @PostMapping
    public ResponseEntity<CheckInResponse> registrar(@Valid @RequestBody CheckInRequest req, Authentication auth) {
        CheckInResponse resposta = checkInService.registrar(auth.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /** Lista o histórico de check-ins do atleta autenticado. */
    @GetMapping("/meus")
    public ResponseEntity<List<CheckInResponse>> listarMeus(Authentication auth) {
        return ResponseEntity.ok(checkInService.listarMeus(auth.getName()));
    }

    /**
     * Lista o elenco com status de risco de cada atleta, para a comissão técnica.
     *
     * @return 200 OK com a lista; 403 se o autenticado não for da comissão técnica
     */
    @GetMapping("/elenco")
    public ResponseEntity<List<AtletaResumoResponse>> listarElenco(Authentication auth) {
        return ResponseEntity.ok(checkInService.listarElenco(auth.getName()));
    }

    /**
     * Lista o histórico completo de check-ins de um atleta específico, para
     * a comissão técnica (usado no gráfico de evolução e histórico detalhado).
     *
     * @return 200 OK com a lista; 403 se o autenticado não for da comissão técnica
     */
    @GetMapping("/atleta/{atletaId}")
    public ResponseEntity<List<CheckInResponse>> listarHistoricoDoAtleta(
            @PathVariable Long atletaId, Authentication auth
    ) {
        return ResponseEntity.ok(checkInService.listarHistoricoDoAtleta(auth.getName(), atletaId));
    }
}
