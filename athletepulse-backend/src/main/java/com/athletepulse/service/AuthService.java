package com.athletepulse.service;

import com.athletepulse.dto.LoginRequest;
import com.athletepulse.dto.LoginResponse;
import com.athletepulse.dto.RegistroRequest;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.UsuarioRepository;
import com.athletepulse.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio de autenticação: cadastro de novos usuários e login.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Cadastra um novo usuário no sistema.
     *
     * @param req dados de cadastro
     * @throws NegocioException se o e-mail já estiver em uso (409) ou o tipo informado for inválido (400)
     */
    @Transactional
    public void registrar(RegistroRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new NegocioException("Este email já está cadastrado.", HttpStatus.CONFLICT);
        }

        TipoUsuario tipo = converterTipo(req.tipo());

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(req.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setTipo(tipo);

        usuarioRepository.save(usuario);
    }

    /**
     * Autentica um usuário e gera seu token JWT.
     * <p>
     * Por segurança, a mensagem de erro é sempre a mesma genérica
     * ("Email ou senha inválidos") tanto para e-mail inexistente, senha
     * incorreta, quanto para perfil declarado incompatível com o cadastrado -
     * isso evita que um atacante descubra quais e-mails existem no sistema.
     *
     * @param req credenciais de login
     * @return token JWT e dados básicos do usuário autenticado
     * @throws NegocioException com status 401 se as credenciais forem inválidas
     */
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.email().trim().toLowerCase())
                // Mensagem genérica de propósito: não revela se o email existe.
                .orElseThrow(() -> new NegocioException("Email ou senha inválidos.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.senha(), usuario.getSenha())) {
            throw new NegocioException("Email ou senha inválidos.", HttpStatus.UNAUTHORIZED);
        }

        if (!usuario.isAtivo()) {
            throw new NegocioException("Esta conta foi desativada. Fale com a comissão técnica.", HttpStatus.FORBIDDEN);
        }

        TipoUsuario perfilInformado = converterTipo(req.perfil());
        if (usuario.getTipo() != perfilInformado) {
            // Ex.: alguém tentando logar como comissão numa conta de jogador.
            throw new NegocioException("Email ou senha inválidos.", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.gerarToken(usuario.getEmail(), usuario.getTipo().name());
        return new LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getTipo().name().toLowerCase());
    }

    /** Converte a string de perfil vinda do front (minúscula) para o enum {@link TipoUsuario}. */
    private TipoUsuario converterTipo(String valor) {
        try {
            return TipoUsuario.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Tipo de usuário inválido.", HttpStatus.BAD_REQUEST);
        }
    }
}
