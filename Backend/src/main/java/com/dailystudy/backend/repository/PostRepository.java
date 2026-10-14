package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {

    // Ordena os posts por data de criacao
    List<Post> findByComentPostIdIsNullOrderByDataCriacaoDesc();

    List<Post> findByAutorIdOrderByDataCriacaoDesc(Long autorId);

    List<Post> findByComentPostIdOrderByDataCriacaoDesc(String comentPostId);

    long countByComentPostId(String comentPostId);
}
