package com.dailystudy.backend.service;

import com.dailystudy.backend.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageUploadServiceTest {

    @Mock
    private OciStorageService ociStorageService;

    @Test
    void upload_imagemValida_enviaComTipoDetectadoEDevolveUrl() throws IOException {
        byte[] png = lerRecurso("/imagens/exemplo.png");
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "foto.png", "text/html", png);
        when(ociStorageService.upload(any(InputStream.class), eq((long) png.length), eq("image/png")))
                .thenReturn("https://exemplo.test/o/abc");
        ImageUploadService servico = new ImageUploadService(ociStorageService);

        String url = servico.upload(arquivo);

        assertEquals("https://exemplo.test/o/abc", url);
    }

    @Test
    void upload_conteudoQueNaoEImagem_lancaExcecaoESemChamarBucket() {
        // Arrange: texto puro com o Content-Type e o nome de um PNG (cliente mentindo)
        byte[] texto = "isto definitivamente nao e uma imagem".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", texto);
        ImageUploadService servico = new ImageUploadService(ociStorageService);

        // Act + Assert
        assertThrows(InvalidImageException.class, () -> servico.upload(arquivo));

        // Nada pode ter sido enviado ao bucket
        verifyNoInteractions(ociStorageService);
    }

    private static byte[] lerRecurso(String caminho) throws IOException {
        try (InputStream in = ImageUploadServiceTest.class.getResourceAsStream(caminho)) {
            assertNotNull(in, "arquivo de teste não encontrado: " + caminho);
            return in.readAllBytes();
        }
    }
}
