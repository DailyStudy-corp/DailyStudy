package com.dailystudy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    private String id;

    private String postId;
    private String autorId;
    private String content;
    private LocalDateTime dataCriacao;
}
