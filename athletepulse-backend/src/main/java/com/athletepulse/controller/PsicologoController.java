package com.athletepulse.controller;

import com.athletepulse.dto.AtletaEmocionalResponse;
import com.athletepulse.dto.NotaRequest;
import com.athletepulse.dto.NotaResponse;
import com.athletepulse.service.PsicologoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/psicologo")
public class PsicologoController {

    private final PsicologoService psicologoService;

    public PsicologoController(PsicologoService psicologoService) {
        this.psicologoService = psicologoService;
    }

    @GetMapping("/atletas")
    public ResponseEntity<List<AtletaEmocionalResponse>> listarAtletas(Authentication auth) {
        return ResponseEntity.ok(psicologoService.listarAtletas(auth.getName()));
    }

    @GetMapping("/atletas/{atletaId}/notas")
    public ResponseEntity<List<NotaResponse>> listarNotas(@PathVariable Long atletaId, Authentication auth) {
        return ResponseEntity.ok(psicologoService.listarNotas(auth.getName(), atletaId));
    }

    @PostMapping("/atletas/{atletaId}/notas")
    public ResponseEntity<NotaResponse> criarNota(
            @PathVariable Long atletaId,
            @Valid @RequestBody NotaRequest req,
            Authentication auth
    ) {
        NotaResponse resposta = psicologoService.criarNota(auth.getName(), atletaId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}
