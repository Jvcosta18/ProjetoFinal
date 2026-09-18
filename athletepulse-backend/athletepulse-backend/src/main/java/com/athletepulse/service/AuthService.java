package com.athletepulse.service;

import com.athletepulse.dto.LoginRequest;
import com.athletepulse.dto.LoginResponse;
import com.athletepulse.dto.RegistroRequest;
import com.athletepulse.dto.RegistroResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.Clube;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.ClubeRepository;
import com.athletepulse.repository.UsuarioRepository;
import com.athletepulse.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Regras de negócio de autenticação: cadastro de novos usuários e login.
 */
@Service
public class AuthService {

    /** Caracteres usados para gerar o token de convite do clube - sem O/0/I/1, que se confundem visualmente. */
    private static final String CARACTERES_TOKEN = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_TOKEN = 8;

    private final UsuarioRepository usuarioRepository;
    private final ClubeRepository clubeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            UsuarioRepository usuarioRepository,
            ClubeRepository clubeRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.clubeRepository = clubeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Cadastra um novo usuário no sistema, associando-o a um clube.
     * <p>
     * Atletas e psicólogos sempre entram num clube já existente, informando
     * o token de convite ({@code req.tokenClube()}). A comissão técnica
     * escolhe: se {@code req.temClube()} for verdadeiro, entra por token
     * como os demais; se for falso, um novo clube é criado com o nome
     * informado ({@code req.nomeClube()}) e um token de convite é gerado -
     * esse token vem na resposta para a pessoa compartilhar com o resto da equipe.
     *
     * @param req dados de cadastro
     * @return nome do clube e, se um clube novo foi criado, o token gerado
     * @throws NegocioException se o e-mail já estiver em uso (409), o tipo
     *                          informado for inválido (400), o token de
     *                          clube não for encontrado (404), ou faltar o
     *                          nome do clube ao criar um novo (400)
     */
    @Transactional
    public RegistroResponse registrar(RegistroRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new NegocioException("Este email já está cadastrado.", HttpStatus.CONFLICT);
        }

        TipoUsuario tipo = converterTipo(req.tipo());

        boolean vaiCriarClube = tipo == TipoUsuario.COMISSAO && Boolean.FALSE.equals(req.temClube());

        Clube clube = vaiCriarClube
                ? criarNovoClube(req.nomeClube())
                : buscarClubePorToken(req.tokenClube());

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(req.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setTipo(tipo);
        usuario.setClube(clube);

        usuarioRepository.save(usuario);

        return new RegistroResponse(clube.getNome(), vaiCriarClube ? clube.getToken() : null);
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

    /** Cria um novo clube com um token de convite único, gerado aleatoriamente. */
    private Clube criarNovoClube(String nomeClube) {
        if (nomeClube == null || nomeClube.isBlank()) {
            throw new NegocioException("Informe o nome do clube.", HttpStatus.BAD_REQUEST);
        }

        Clube clube = new Clube();
        clube.setNome(nomeClube.trim());
        clube.setToken(gerarTokenUnico());

        return clubeRepository.save(clube);
    }

    /** Busca um clube pelo token de convite informado no cadastro. */
    private Clube buscarClubePorToken(String tokenClube) {
        if (tokenClube == null || tokenClube.isBlank()) {
            throw new NegocioException("Informe o token do clube.", HttpStatus.BAD_REQUEST);
        }

        return clubeRepository.findByToken(tokenClube.trim().toUpperCase())
                .orElseThrow(() -> new NegocioException("Token de clube inválido.", HttpStatus.NOT_FOUND));
    }

    /** Gera um código de convite aleatório de 8 caracteres, garantindo que não colida com um já existente. */
    private String gerarTokenUnico() {
        String token;
        do {
            StringBuilder sb = new StringBuilder(TAMANHO_TOKEN);
            for (int i = 0; i < TAMANHO_TOKEN; i++) {
                sb.append(CARACTERES_TOKEN.charAt(random.nextInt(CARACTERES_TOKEN.length())));
            }
            token = sb.toString();
        } while (clubeRepository.existsByToken(token));

        return token;
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
