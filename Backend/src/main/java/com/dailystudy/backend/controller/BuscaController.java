package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.HistoricoBuscaDTO;
import com.dailystudy.backend.dto.ResultadoBuscaDTO;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.service.HistoricoBuscaService;
import com.dailystudy.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class BuscaController {

    private final HistoricoBuscaService historicoBuscaService;

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ResultadoBuscaDTO> buscar(@RequestParam("q") String q, @AuthenticationPrincipal Usuario usuarioLogado){
        ResultadoBuscaDTO resultado = searchService.buscar(q);

        if (resultado.totalResultados() > 0){
            historicoBuscaService.registrarBusca(usuarioLogado.getId(), q);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoBuscaDTO>> listarHistorico(@AuthenticationPrincipal Usuario usuarioLogado) {

        List<HistoricoBuscaDTO> historico = historicoBuscaService.listarRecentes(usuarioLogado.getId())
                .stream()
                .map(HistoricoBuscaDTO::new)
                .toList();

        return ResponseEntity.ok(historico);
    }
}
