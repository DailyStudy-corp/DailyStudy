package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.Post;

import java.time.LocalDateTime;

public record PostFeedDTO(
        String id,
        String content,
        String mediaUrl,
        Long autorId,
        String autorUsername,
        String autorImg,
        LocalDateTime dataCriacao,
        LocalDateTime dataEdicao,
        String comentPostId,
        long totalCurtidas,
        long totalComentarios) {

    public PostFeedDTO(Post post, String autorUsername, String autorImg, long totalCurtidas, long totalComentarios){
        this(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getAutorId(),
                autorUsername,
                autorImg,
                post.getDataCriacao(),
                post.getDataEdicao(),
                post.getComentPostId(),
                totalCurtidas,
                totalComentarios
        );
    }
}
