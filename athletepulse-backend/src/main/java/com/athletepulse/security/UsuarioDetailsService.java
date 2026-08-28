package com.athletepulse.security;

import com.athletepulse.model.Usuario;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação do {@link UserDetailsService} do Spring Security que carrega
 * os dados do usuário a partir do banco (tabela {@code usuarios}), usando o
 * e-mail como identificador.
 * <p>
 * O perfil ({@link com.athletepulse.model.TipoUsuario}) é mapeado como "role"
 * do Spring Security (ex: {@code ROLE_JOGADOR}), embora este projeto faça a
 * verificação de permissões manualmente nos services em vez de usar
 * {@code @PreAuthorize}.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carrega um usuário pelo e-mail para uso interno do Spring Security
     * (autenticação via {@link com.athletepulse.security.JwtAuthFilter}).
     *
     * @param email e-mail do usuário
     * @return dados de autenticação do usuário
     * @throws UsernameNotFoundException se não existir usuário com esse e-mail
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(usuario.getTipo().name())
                .build();
    }
}
