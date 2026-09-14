package com.dailystudy.backend.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "indice_busca")
@CompoundIndexes({
        // Índice composto único: garante no banco que a mesma (termo, refId, campo)
        // nunca existe duas vezes — rede de segurança pra estratégia de
        // "apagar e recriar" da reindexação
        // Como termo é o primeiro campo do índice, ele também serve pra
        // acelerar a busca "todas as entradas com esse termo".
        @CompoundIndex(name = "uk_termo_ref_campo", def = "{'termo': 1, 'refId': 1}", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndiceBusca {

    @Id
    private String id;

    private String termo;

    private TipoReferencia tipo;

    @Indexed
    private String refId;

    private CampoIndexado campo;

    private LocalDateTime atualizadoEm;
}
