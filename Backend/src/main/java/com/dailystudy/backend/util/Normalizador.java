package com.dailystudy.backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class Normalizador {

    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}");

    private Normalizador() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD);
        return MARCAS_DE_ACENTO.matcher(semAcento).replaceAll("");
    }
}
