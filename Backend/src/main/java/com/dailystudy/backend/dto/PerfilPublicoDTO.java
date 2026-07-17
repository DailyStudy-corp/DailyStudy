package com.dailystudy.backend.dto;

import java.util.List;

public record PerfilPublicoDTO(
        String username,
        String img_perfil,
        String banner_perfil,
        String cargo,
        String bio,
        List<PostFeedDTO> posts
) {

}
