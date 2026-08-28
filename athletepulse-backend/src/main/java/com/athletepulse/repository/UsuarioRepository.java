package com.athletepulse.repository;

import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de {@link Usuario}.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /** Busca um usuário pelo e-mail (usado no login e na autenticação via JWT). */
    Optional<Usuario> findByEmail(String email);
    /** Verifica se já existe um usuário cadastrado com o e-mail informado. */
    boolean existsByEmail(String email);
    /** Lista todos os usuários de um determinado perfil, ordenados por nome. */
    List<Usuario> findByTipoOrderByNomeAsc(TipoUsuario tipo);
}
