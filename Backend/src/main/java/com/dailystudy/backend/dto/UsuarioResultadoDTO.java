package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.Usuario;

public record UsuarioResultadoDTO(
        Long id,
        String username,
        String img_perfil,
        String cargo,
        String bio,
        int score
) {
    public UsuarioResultadoDTO(Usuario usuario, int score) {
        this(usuario.getId(), usuario.getUsername(), usuario.getImg_perfil(),
                usuario.getCargo(), usuario.getBio(), score);
    }
}
