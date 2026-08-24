package com.dailystudy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.awt.*;
import java.time.LocalDateTime;

@Document(collection = "posts")
@CompoundIndex(name = "autorId_dataCriacao_idx", def = "{'autorId': 1, 'dataCriacao': -1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    private String id;
    private String content;
    private String mediaUrl;
    private String visibility;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataEdicao;
    private Long autorId;

    private String comentPostId;
}
