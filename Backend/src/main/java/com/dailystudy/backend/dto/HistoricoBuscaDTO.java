package com.dailystudy.backend.dto;

import com.dailystudy.backend.model.HistoricoBusca;

import java.time.LocalDateTime;

public record HistoricoBuscaDTO(String termo, LocalDateTime dataBusca) {

    public HistoricoBuscaDTO(HistoricoBusca historico) {
        this(historico.getTermo(), historico.getDataBusca());
    }
}
