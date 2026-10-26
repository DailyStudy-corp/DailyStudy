package com.dailystudy.backend.util;

import com.dailystudy.backend.exception.InvalidImageException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageValidator {
    static final int TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final String MENSAGEM_IMAGEM_INVALIDA = "Imagem corrompida ou inválida";

    public static String validar(byte [] conteudo){
        if (conteudo.length > TAMANHO_MAXIMO_BYTES) {
            throw new InvalidImageException("Imagem grande demais, tamanho máximo 5MB");
        }

        String tipo = ImageTypeDetector.detectar(conteudo)
                .orElseThrow(() -> new InvalidImageException("Formato de imagem não suportado. Suporte para PNG, JPEG, GIF ou Webp"));

        if (tipo.equals("image/webp")) {
            return tipo;
        }

        try {
            if (ImageIO.read(new ByteArrayInputStream(conteudo)) == null) {
                throw new InvalidImageException(MENSAGEM_IMAGEM_INVALIDA);
            }
        } catch (IOException e) {
            throw new InvalidImageException(MENSAGEM_IMAGEM_INVALIDA, e);
        }

        return tipo;
    }

}
