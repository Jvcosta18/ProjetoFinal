package com.athletepulse.repository;

import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByTipoOrderByNomeAsc(TipoUsuario tipo);
}
