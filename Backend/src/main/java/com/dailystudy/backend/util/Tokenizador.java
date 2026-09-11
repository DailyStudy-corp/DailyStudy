package com.dailystudy.backend.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Tokenizador {

    private static final int TAMANHO_MINIMO_TERMO = 3;

    private Tokenizador(){
    }

    public static Set<String> tokenizar(String texto) {
        String normalizado = Normalizador.normalizar(texto);

        if (normalizado.isBlank()){
            return Set.of();
        }

        return Arrays.stream(normalizado.split("[^a-z0-9]+"))
                .filter(termo -> termo.length() >= TAMANHO_MINIMO_TERMO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
