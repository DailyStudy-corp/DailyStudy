package com.dailystudy.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageTypeDetectorTest {

    @Test
    void detectar_pngValido_retornaImagemPng() {
        byte[] conteudo = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 0
        };

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.of("image/png"), resultado);
    }

    @Test
    void detectar_jpegValido_retornaImagemJpeg() {
        byte[] conteudo = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0, 0, 0, 0, 0, 0, 0, 0};

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.of("image/jpeg"), resultado);
    }

    @Test
    void detectar_gif89aValido_retornaGif() {
        byte[] conteudo = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61,
        0, 0, 0, 0, 0, 0};

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.of("image/gif"), resultado);
    }

    @Test
    void detectar_gif87aValido_retornaGif() {
        byte[] conteudo = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61,
                0, 0, 0, 0, 0, 0};

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.of("image/gif"), resultado);
    }

    @Test
    void detectar_webpValido_retornaImageWebp() {
        byte[] conteudo = new byte[] {
                0x52, 0x49, 0x46, 0x46,   // "RIFF"
                0x08, 0x00, 0x00, 0x00,   // tamanho: 16 - 8 = 8
                0x57, 0x45, 0x42, 0x50,   // "WEBP"
                0, 0, 0, 0                // dados
        };

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.of("image/webp"), resultado);
    }

    @Test
    void detectar_wavComCabecalhoRiff_retornaVazio() {
        byte[] conteudo = new byte[] {
                0x52, 0x49, 0x46, 0x46,   // "RIFF"
                0x08, 0x00, 0x00, 0x00,   // tamanho: 16 - 8 = 8 (correto, de propósito)
                0x57, 0x41, 0x56, 0x45,   // "WAVE" (e não "WEBP")
                0, 0, 0, 0                // dados
        };

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.empty(), resultado);
    }

    @Test
    void detectar_webpComTamanhoErrado_retornaVazio() {
        byte[] conteudo = new byte[] {
                0x52, 0x49, 0x46, 0x46,   // "RIFF"
                0x09, 0x00, 0x00, 0x00,   // tamanho declarado: 9 (o real seria 8)
                0x57, 0x45, 0x42, 0x50,   // "WEBP"
                0, 0, 0, 0                // dados
        };

        Optional<String> resultado = ImageTypeDetector.detectar(conteudo);

        assertEquals(Optional.empty(), resultado);
    }

    @Test
    void detectar_null_retornaVazio() {
        assertEquals(Optional.empty(), ImageTypeDetector.detectar(null));
    }

    @Test
    void detectar_arrayCurto_retornaVazio() {
        byte[] conteudo = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,   // assinatura PNG completa
                0, 0, 0                                                   // só 11 bytes no total
        };

        assertEquals(Optional.empty(), ImageTypeDetector.detectar(conteudo));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "/imagens/exemplo-lossy.webp",
            "/imagens/exemplo-lossless.webp",
            "/imagens/exemplo-alfa.webp"
    })
    void detectar_webpReal_retornaImageWebp(String caminho) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            assertNotNull(in, "arquivo de teste não encontrado" + caminho);
            byte[] conteudo = in.readAllBytes();

            assertEquals(Optional.of("image/webp"), ImageTypeDetector.detectar(conteudo));
        }
    }

}
