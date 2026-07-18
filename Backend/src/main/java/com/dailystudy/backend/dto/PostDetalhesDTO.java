package com.dailystudy.backend.dto;

import java.util.List;

public record PostDetalhesDTO(
        PostFeedDTO post,
        List<PostFeedDTO> comentarios) {
}