package com.dailystudy.backend.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public final class ImageTypeDetector {

    private static final int TAMANHO_MINIMO = 12;
    private static final byte[] ASSINATURA_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] ASSINATURA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ASSINATURA_GIF89A = {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61};
    private static final byte[] ASSINATURA_GIF87A = {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61};
    private static final byte[] ASSINATURA_RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ASSINATURA_WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int POSICAO_TAMANHO_RIFF = 4;
    private static final int POSICAO_WEBP = 8;
    private static final int BYTES_NAO_CONTADOS_NO_TAMANHO = 8;


    private ImageTypeDetector() {
    }

    public static Optional<String> detectar(byte[] conteudo) {
        if (conteudo == null || conteudo.length < TAMANHO_MINIMO) {
            return Optional.empty();
        }

        if (comecaCom(conteudo, ASSINATURA_PNG)){
            return Optional.of("image/png");
        }

        if (comecaCom(conteudo, ASSINATURA_JPEG)){
            return Optional.of("image/jpeg");
        }

        if (comecaCom(conteudo, ASSINATURA_GIF89A) || comecaCom(conteudo, ASSINATURA_GIF87A)){
            return Optional.of("image/gif");
        }

        if (ehWebp(conteudo)){
            return Optional.of("image/webp");
        }



        return Optional.empty();
    }

    private static boolean comecaCom(byte[] conteudo, byte[] assinatura){
        return temNaPosicao(conteudo, 0, assinatura);
    }

    private static boolean temNaPosicao(byte[] conteudo, int posicao, byte[] assinatura){
        return Arrays.equals(conteudo, posicao, posicao + assinatura.length,
                            assinatura, 0, assinatura.length);
    }

    private static boolean ehWebp(byte[] conteudo) {
        if (!comecaCom(conteudo, ASSINATURA_RIFF) || !temNaPosicao(conteudo, POSICAO_WEBP, ASSINATURA_WEBP)){
            return false;
        }

        int tamanhoDeclarado = ByteBuffer.wrap(conteudo)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt(POSICAO_TAMANHO_RIFF);

        return tamanhoDeclarado == conteudo.length - BYTES_NAO_CONTADOS_NO_TAMANHO;
    }

}
