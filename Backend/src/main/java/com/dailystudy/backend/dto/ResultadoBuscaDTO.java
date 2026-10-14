package com.dailystudy.backend.dto;

import java.util.List;

public record ResultadoBuscaDTO(
        List<UsuarioResultadoDTO> usuarios,
        List<PostResultadoDTO> posts
) {
    public int totalResultados() {
        return usuarios.size() + posts().size();
    }
}
