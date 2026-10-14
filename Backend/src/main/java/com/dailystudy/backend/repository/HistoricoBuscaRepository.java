package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.HistoricoBusca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoBuscaRepository extends JpaRepository<HistoricoBusca, Long> {

    Optional<HistoricoBusca> findByUsuarioIdAndTermo(Long usuarioId, String termo);

    // Top 10 é termo traduzido do Spring para retornar no máximo 10 linhas do banco de dados
    List<HistoricoBusca> findTop10ByUsuarioIdOrderByDataBuscaDesc(Long usuarioId);
}
