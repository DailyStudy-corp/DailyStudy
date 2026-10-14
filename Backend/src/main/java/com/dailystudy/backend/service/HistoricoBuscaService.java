package com.dailystudy.backend.service;

import com.dailystudy.backend.model.HistoricoBusca;
import com.dailystudy.backend.repository.HistoricoBuscaRepository;
import com.dailystudy.backend.util.Normalizador;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoBuscaService {

    private final HistoricoBuscaRepository historicoBuscaRepository;

    // \p{M} = categoria Unicode "Mark" — é o que sobra de um acento depois
    // que o NFD abaixo separa a letra do acento. Mesma ideia do regex
    // /[\u0300-\u036f]/g que o search.js já usa no frontend.
    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}");

    @Transactional
    public void registrarBusca(Long usuarioId, String termoBruto) {

        String termo = Normalizador.normalizar(termoBruto);

        if (termo.isBlank()) {
            return;
        }

        HistoricoBusca historico = historicoBuscaRepository
                .findByUsuarioIdAndTermo(usuarioId, termo)
                .orElseGet(() -> { //orElseGet aqui é especifico, ao inves de sempre um INSERT ele vira um UPDATE
                    HistoricoBusca novo = new HistoricoBusca();
                    novo.setUsuarioId(usuarioId);
                    novo.setTermo(termo);
                    return novo;
                });
        historico.setDataBusca(LocalDateTime.now());
        historicoBuscaRepository.save(historico);

        log.info("Historico de busca registrado: usuarioId={}, termo={}", usuarioId, termo);
    }

    public List<HistoricoBusca> listarRecentes(Long usuarioId) {
        return historicoBuscaRepository.findTop10ByUsuarioIdOrderByDataBuscaDesc(usuarioId);
    }

    private String normalizar(String termo) {
        if (termo == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(termo.trim().toLowerCase(), Normalizer.Form.NFD);
        return MARCAS_DE_ACENTO.matcher(semAcento).replaceAll("");
    }
}
