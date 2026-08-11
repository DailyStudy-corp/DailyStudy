package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Comentario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComentarioRepository extends MongoRepository<Comentario, String> {

    List<Comentario> findByPostId(String postId);
    long countByPostId(String postId);
}
