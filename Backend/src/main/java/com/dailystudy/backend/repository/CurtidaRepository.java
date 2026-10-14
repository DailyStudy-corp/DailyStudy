package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Curtida;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CurtidaRepository extends MongoRepository<Curtida, String>, CurtidaRepositoryCustom {

    long countByPostId(String postId);

    Optional<Curtida> findByPostIdAndAutorId(String postId, Long autorId);
}
