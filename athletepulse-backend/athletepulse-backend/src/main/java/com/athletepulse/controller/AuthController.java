package com.athletepulse.controller;

import com.athletepulse.dto.LoginRequest;
import com.athletepulse.dto.LoginResponse;
import com.athletepulse.dto.RegistroRequest;
import com.athletepulse.dto.RegistroResponse;
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
     * @return 201 Created com o nome do clube (e o token, se um clube novo
     *         foi criado); 409 se o e-mail já existir; 404 se o token de
     *         clube informado não existir
     */
    @PostMapping("/registrar")
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(req));
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
