package com.dailystudy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarPostDTO(
        @NotBlank(message = "O Conteúdo não pode estar vazio")
        @Size(max = 500, message = "O Conteúdo pode conter no máximo 500 caracteres")
        String content,
        String mediaUrl) {
}
