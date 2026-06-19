package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.model.UsuarioRole;

public record UsuarioResponseDTO(Long id, String username, String img_perfil,String banner_perfil, String cargo, String bio, UsuarioRole role) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getUsername(), usuario.getImg_perfil(), usuario.getBanner_perfil(), usuario.getCargo(), usuario.getBio(), usuario.getRole());
    }
}
