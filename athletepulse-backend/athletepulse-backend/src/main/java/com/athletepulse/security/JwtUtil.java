package com.athletepulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Componente responsável por gerar e validar tokens JWT usados na autenticação stateless da API.
 * <p>
 * O segredo de assinatura e o tempo de expiração vêm de {@code application.properties}
 * ({@code app.jwt.secret} e {@code app.jwt.expiracao-ms}).
 */
@Component
public class JwtUtil {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String segredo,
            @Value("${app.jwt.expiracao-ms}") long expiracaoMs
    ) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    /**
     * Gera um novo token JWT para um usuário autenticado.
     *
     * @param email e-mail do usuário, usado como subject do token
     * @param tipo  perfil do usuário, incluído como claim customizada
     * @return token JWT assinado, pronto para ser enviado ao front-end
     */
    public String gerarToken(String email, String tipo) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        return Jwts.builder()
                .subject(email)
                .claims(Map.of("tipo", tipo))
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    /**
     * Extrai o e-mail (subject) contido em um token já validado.
     *
     * @param token token JWT
     * @return e-mail do usuário dono do token
     */
    public String extrairEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Verifica se um token é válido: assinatura correta e ainda não expirado.
     *
     * @param token token JWT a validar
     * @return {@code true} se o token é válido; {@code false} caso contrário (inválido, expirado ou malformado)
     */
    public boolean tokenValido(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
