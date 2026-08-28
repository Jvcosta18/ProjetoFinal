package com.athletepulse.controller;

import com.athletepulse.dto.LoginRequest;
import com.athletepulse.dto.LoginResponse;
import com.athletepulse.dto.RegistroRequest;
import com.athletepulse.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticação (cadastro e login).
 * <p>
 * Único controller cujas rotas não exigem token JWT - liberado em
 * {@link com.athletepulse.config.SecurityConfig}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Cadastra um novo usuário.
     *
     * @return 201 Created em caso de sucesso; 409 se o e-mail já existir
     */
    @PostMapping("/registrar")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroRequest req) {
        authService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Autentica um usuário e retorna o token JWT.
     *
     * @return 200 OK com o token; 401 se as credenciais forem inválidas
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
