package com.athletepulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// "tipo" chega como string minúscula do <select> do front ("jogador" | "comissao")
// e é convertido pro enum TipoUsuario dentro do UsuarioService.
public record RegistroRequest(
        @Size(min = 3, max = 120, message = "Nome deve ter pelo menos 3 caracteres")
        String nome,

        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        String senha
) {}
