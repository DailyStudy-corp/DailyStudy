package com.dailystudy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "curtida")
@CompoundIndex(name = "postId_autorId_idx", def = "{'postId': 1, 'autorId': 1}", unique = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Curtida {

    @Id
    private String id;

    private Long autorId;
    private String postId;
    private LocalDateTime dataCriacao;
}
