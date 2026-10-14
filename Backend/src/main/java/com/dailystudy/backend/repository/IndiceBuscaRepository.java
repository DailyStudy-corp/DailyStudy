package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.IndiceBusca;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IndiceBuscaRepository extends MongoRepository<IndiceBusca, String> {

    // Reindexação ("apagar e recriar"): remove todas as entradas de um
    // post/comentário/usuário antes de inserir as novas.
    void deleteByRefId(String refId);

    // Busca: acha todas as entradas cujo termo está na lista de termos
    // digitados pelo usuário — é essa query que faz o índice funcionar
    // como índice de verdade (lookup direto, não scan).
    List<IndiceBusca> findByTermoIn(Collection<String> termos);
}

