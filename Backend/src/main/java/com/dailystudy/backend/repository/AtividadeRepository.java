package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Atividade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepository extends MongoRepository<Atividade, String> {

    List<Atividade> findAllByOrderByDataCriacaoDesc();
}

