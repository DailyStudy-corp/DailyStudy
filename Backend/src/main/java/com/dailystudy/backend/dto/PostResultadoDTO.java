package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.Post;

import java.time.LocalDateTime;

public record PostResultadoDTO(
        String id,
        String content,
        String mediaUrl,
        LocalDateTime dataCriacao,
        Long autorId,
        String autorUsername,
        int score
) {
    public PostResultadoDTO(Post post,String autorUsername, int score) {
        this(post.getId(), post.getContent(), post.getMediaUrl(),
                post.getDataCriacao(), post.getAutorId(), autorUsername, score);
    }
}
