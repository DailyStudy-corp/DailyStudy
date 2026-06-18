package com.dailystudy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "atividade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atividade {

    //AtivID é a chave primaria das atividades que aconteceram no post, por exemplo um like é uma atividade, um comentario é outro
    private String ativId;
    private String autorId;
    private String status;

    //Diferente do AtivID, ele é a chave primaria do Post, todas atividades de um post apontam para mesma chave do post
    private String objetoId;

    //Aqui ele inicia o map para retornar os dados rapidos do Banco relacional sem que tenha que fazer uma completa
    private Map<String, Object> metadata = new HashMap<>();
    private LocalDateTime dataCriacao;

}
