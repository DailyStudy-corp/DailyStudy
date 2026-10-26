package com.dailystudy.backend.util;

import com.dailystudy.backend.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    @Test
    void validar_pngReal_retornaImagemPng() throws IOException {
        byte[] conteudo = lerRecurso("/imagens/exemplo.png");

        String tipo = ImageValidator.validar(conteudo);

        assertEquals("image/png", tipo);
    }

    @Test
    void validar_jpegReal_retornaImagemJpg() throws IOException {
        byte[] conteudo = lerRecurso("/imagens/exemplo.jpg");

        String tipo = ImageValidator.validar(conteudo);

        assertEquals("image/jpeg", tipo);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "/imagens/exemplo-lossy.webp",
            "/imagens/exemplo-lossless.webp",
            "/imagens/exemplo-alfa.webp"
    })
    void validar_webpReal_retornaImagemWebp(String caminho) throws IOException {
        byte[] conteudo = lerRecurso(caminho);

        String tipo = ImageValidator.validar(conteudo);

        assertEquals("image/webp", tipo);
    }

    @Test
    void validar_gifReal_retornaImagemGif() throws IOException {
        byte[] conteudo = lerRecurso("/imagens/exemplo.gif");

        String tipo = ImageValidator.validar(conteudo);

        assertEquals("image/gif", tipo);
    }

    @Test
    void validar_ConteudoQueNaoEImagem_Excecao() throws IOException {
        byte[] conteudo = "isto nao e uma imagem".getBytes(StandardCharsets.US_ASCII);

        InvalidImageException erro = assertThrows(InvalidImageException.class,
                () -> ImageValidator.validar(conteudo));

        assertEquals("Formato de imagem não suportado. Suporte para PNG, JPEG, GIF ou Webp", erro.getMessage());
    }

    @Test
    void validar_ConteudoAcima_5MB() throws IOException {
        byte[] png = lerRecurso("/imagens/exemplo.png");
        byte[] conteudo = Arrays.copyOf(png, ImageValidator.TAMANHO_MAXIMO_BYTES + 1);

        InvalidImageException erro = assertThrows(InvalidImageException.class,
                () -> ImageValidator.validar(conteudo));

        assertEquals("Imagem grande demais, tamanho máximo 5MB", erro.getMessage());
    }

    @Test
    void validar_conteudoNoLimiteExato_aceita() throws IOException {
        byte[] png = lerRecurso("/imagens/exemplo.png");
        byte[] conteudo = Arrays.copyOf(png, ImageValidator.TAMANHO_MAXIMO_BYTES);

        String tipo = ImageValidator.validar(conteudo);

        assertEquals("image/png", tipo);
    }

    @Test
    void validar_cabecalhoPngComLixo_lancaExcecaoComMotivo() throws IOException{
        byte[] conteudo = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x47, 0x0D, 0x0A, 0x1A};

        InvalidImageException erro = assertThrows(InvalidImageException.class,
                () -> ImageValidator.validar(conteudo));

        assertEquals("Imagem corrompida ou inválida", erro.getMessage());
    }

    private static byte[] lerRecurso(String caminho) throws IOException {
        try (InputStream in = ImageValidatorTest.class.getResourceAsStream(caminho)) {
            assertNotNull(in, "arquivo de teste não encontrado: " + caminho);
            return in.readAllBytes();
        }
    }
}
