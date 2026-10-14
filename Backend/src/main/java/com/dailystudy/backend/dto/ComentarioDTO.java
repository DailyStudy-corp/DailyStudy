package com.dailystudy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioDTO(@NotBlank(message = "O conteúdo não pode estar vazio")
                            @Size(max = 500, message = "O conteudo deve ter no máximo 500 caracteres")
                            String content,
                            String mediaUrl) {
}
