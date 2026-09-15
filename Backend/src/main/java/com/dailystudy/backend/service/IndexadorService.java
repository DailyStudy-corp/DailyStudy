package com.dailystudy.backend.service;

import com.dailystudy.backend.model.CampoIndexado;
import com.dailystudy.backend.model.IndiceBusca;
import com.dailystudy.backend.model.TipoReferencia;
import com.dailystudy.backend.repository.IndiceBuscaRepository;
import com.dailystudy.backend.util.Tokenizador;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexadorService {

    private final IndiceBuscaRepository indiceBuscaRepository;

    // "Apagar e recriar": remove tudo que já existia pra esse refId e insere
    // os termos atuais. `campos` são os textos a indexar, já rotulados por
    // CampoIndexado (ex: Map.of(CampoIndexado.USERNAME, usuario.getUsername())).
    public void reindexar(String refId, TipoReferencia tipo, Map<CampoIndexado, String> campos){
        try {
            indiceBuscaRepository.deleteByRefId(refId);

            List<IndiceBusca> novasEntradas = new ArrayList<>();
            LocalDateTime agora = LocalDateTime.now();

            for (Map.Entry<CampoIndexado, String> entrada : campos.entrySet()) {
                Set<String> termos = Tokenizador.tokenizar(entrada.getValue());

                for (String termo : termos) {
                    IndiceBusca indice = new IndiceBusca();
                    indice.setTermo(termo);
                    indice.setTipo(tipo);
                    indice.setRefId(refId);
                    indice.setCampo(entrada.getKey());
                    indice.setAtualizadoEm(agora);
                    novasEntradas.add(indice);
                }
            }

            if (!novasEntradas.isEmpty()) {
                indiceBuscaRepository.saveAll(novasEntradas);
            }

            log.info("Índice atualizado: tipo={}, refId={}, termos={}", tipo, refId, novasEntradas.size());

        } catch (Exception e) {
            // Escrita best-effort: a escrita principal (Post/Usuario) já foi
            // commitada antes desse método ser chamado — não faz sentido
            // propagar essa exceção e derrubar a resposta HTTP por causa do
            // índice.
            log.error("Falha ao reindexar tipo={}, refId={}", tipo, refId, e);
        }
    }

    public void removerIndice(String refId) {
        try {
            indiceBuscaRepository.deleteByRefId(refId);
            log.info("Índice removido: refId{}", refId);

        } catch (Exception e) {
            log.error("Falha ao remover índice de refId={}", refId, e);
        }
    }
}
