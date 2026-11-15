package com.dailystudy.backend.util;

import com.dailystudy.backend.exception.InvalidImageException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageValidator {
    static final int TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    static final long PIXELS_MAXIMOS = 16_000_000L;
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
            validarDimensoes(conteudo);

            if (ImageIO.read(new ByteArrayInputStream(conteudo)) == null) {
                throw new InvalidImageException(MENSAGEM_IMAGEM_INVALIDA);
            }
        } catch (IOException e) {
            throw new InvalidImageException(MENSAGEM_IMAGEM_INVALIDA, e);
        }

        return tipo;
    }

    public static void validarDimensoes(byte[] conteudo) throws IOException{
        try (ImageInputStream fluxo = new MemoryCacheImageInputStream(new ByteArrayInputStream(conteudo))) {
            Iterator<ImageReader> leitores = ImageIO.getImageReaders(fluxo);

            if (!leitores.hasNext()) {
                throw new InvalidImageException(MENSAGEM_IMAGEM_INVALIDA);
            }

            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(fluxo);
                long pixels = (long) leitor.getWidth(0) * leitor.getHeight(0);

                if (pixels > PIXELS_MAXIMOS) {
                    throw new InvalidImageException("Imagem com dimensões grandes demais");
                }
            } finally {
                leitor.dispose();
            }
        }
    }

}
