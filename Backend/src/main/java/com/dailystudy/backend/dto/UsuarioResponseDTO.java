package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.model.UsuarioRole;

public record UsuarioResponseDTO(Long id, String username, UsuarioRole role) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getUsername(), usuario.getRole());
    }
}
