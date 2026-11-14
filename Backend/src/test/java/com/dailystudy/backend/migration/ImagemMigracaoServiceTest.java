package com.dailystudy.backend.migration;

import com.dailystudy.backend.exception.InvalidImageException;
import com.dailystudy.backend.service.OciStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImagemMigracaoServiceTest {

    private static final String PNG_1X1 =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Mock
    private OciStorageService storage;

    @InjectMocks
    private ImagemMigracaoService service;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "https://objectstorage.sa-saopaulo-1.oraclecloud.com/n/grnkbnebxpin/b/dailystudy-media-dev/o/3f2b8c1e-5a7d-4e9f-8b6a-1c2d3e4f5a6b"
    })
    void migrar_valorQueNaoPrecisaMigrar_devolveVazioENaoTocaNoBucket(String valor) {
        Optional<String> resultado = service.migrar(valor);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(storage);
    }

    @Test
    void migrar_dataUriValida_sobeParaOBucketEDevolveAUrlNova() {
        String urlNova = "https://exemplo.test/o/abc";
        when(storage.upload(any(InputStream.class), anyLong(), eq("image/png"))).thenReturn(urlNova);

        Optional<String> resultado = service.migrar(PNG_1X1);

        assertEquals(Optional.of(urlNova), resultado);
    }

    @Test
    void migrar_dataUriComBytesQueNaoSaoImagem_lancaExcecaoENaoSobeNada() {
        String naoEhImagem = "data:image/png;base64," +
                Base64.getEncoder().encodeToString("isto não é um png".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidImageException.class, () -> service.migrar(naoEhImagem));
        verifyNoInteractions(storage);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "https://objectstorage.sa-saopaulo-1.oraclecloud.com/n/grnkbnebxpin/b/dailystudy-media-dev/o/3f2b8c1e-5a7d-4e9f-8b6a-1c2d3e4f5a6b"
    })
    void precisaMigrar_valorVazioOuJaMigrado_devolveFalse(String valor) {
        assertFalse(service.precisaMigrar(valor));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            PNG_1X1,
            "http://inseguro.test/foto.png"
    })
    void precisaMigrar_dataUriOuValorDesconhecido_devolveTrue(String valor) {
        assertTrue(service.precisaMigrar(valor));
    }

    @Test
    void validar_dataUriValida_naoLancaENaoTocaNoBucket() {
        assertDoesNotThrow(() -> service.validar(PNG_1X1));
        verifyNoInteractions(storage);
    }

    @Test
    void validar_bytesQueNaoSaoImagem_lancaExcecaoENaoTocaNoBucket() {
        String naoEhImagem = "data:image/png;base64," +
                Base64.getEncoder().encodeToString("isto não é um png".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidImageException.class, () -> service.validar(naoEhImagem));
        verifyNoInteractions(storage);
    }
}
