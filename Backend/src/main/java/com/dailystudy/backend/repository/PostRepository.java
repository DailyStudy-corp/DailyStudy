package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {

    //  metodos 
    List<Post> findByComentPostIdIsNullOrderByDataCriacaoDesc();
    List<Post> findByAutorIdOrderByDataCriacaoDesc(Long autorId);
    List<Post> findByAutorAndComentPostIdIsNullOrderByDataCriacaoDesc(String autor);
    List<Post> findByAutorAndComentPostIdIsNotNullOrderByDataCriacaoDesc(String autor);
    List<Post> findByComentPostIdOrderByDataCriacaoDesc(String comentPostId); 

    long countByComentPostId(String comentPostId);
}
