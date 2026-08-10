package com.athletepulse.controller;

import com.athletepulse.dto.CheckInRequest;
import com.athletepulse.dto.CheckInResponse;
import com.athletepulse.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseEntity<CheckInResponse> registrar(@Valid @RequestBody CheckInRequest req, Authentication auth) {
        CheckInResponse resposta = checkInService.registrar(auth.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<CheckInResponse>> listarMeus(Authentication auth) {
        return ResponseEntity.ok(checkInService.listarMeus(auth.getName()));
    }
}
