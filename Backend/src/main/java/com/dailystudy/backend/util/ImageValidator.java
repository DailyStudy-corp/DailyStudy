package com.dailystudy.backend.util;

import com.dailystudy.backend.exception.InvalidImageException;

public class ImageValidator {
    static final int TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;

    public static String validar(byte [] conteudo){
        if (conteudo.length > TAMANHO_MAXIMO_BYTES) {
            throw new InvalidImageException("Imagem grande demais, tamanho máximo 5MB");
        }
        return ImageTypeDetector.detectar(conteudo)
                .orElseThrow(() -> new InvalidImageException("Formato de imagem não suportado. Suporte para PNG, JPEG, GIF ou Webp"));
    }

}
