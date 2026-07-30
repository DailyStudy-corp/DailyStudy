package com.dailystudy.backend.dto;

import java.util.List;

public class PostDetalhesDTO {

    private PostFeedDTO post;
    private List<PostFeedDTO> comentarios;

    public PostDetalhesDTO() {
    }

    public PostDetalhesDTO(PostFeedDTO post, List<PostFeedDTO> comentarios) {
        this.post = post;
        this.comentarios = comentarios;
    }

    public PostFeedDTO getPost() {
        return post;
    }

    public void setPost(PostFeedDTO post) {
        this.post = post;
    }

    public List<PostFeedDTO> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<PostFeedDTO> comentarios) {
        this.comentarios = comentarios;
    }
}