package com.dailystudy.backend.util;

import com.dailystudy.backend.config.OciStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUrlValidatorTest {

    private static final String BASE =
            "https://objectstorage.sa-saopaulo-1.oraclecloud.com/n/ns-teste/b/bucket-teste/o/";

    private static ImageUrlValidator criarValidador() {
        OciStorageProperties propriedades =
                new OciStorageProperties("config", "DEFAULT", "ns-teste", "bucket-teste", BASE);
        return new ImageUrlValidator(propriedades);
    }

    @Test
    void isSafeImage_urlDoBucketComUuid_ehValida() {
        String url = BASE + "16cdde0e-6bc4-451c-8b23-def5094c3152";

        assertTrue(criarValidador().isSafeImage(url));
    }

    @Test
    void isSafeImage_httpsDeOutroDominio_eRecusada() {
        String url = "https://exemplo.com/imagem.png";

        assertFalse(criarValidador().isSafeImage(url));
    }

    @Test
    void isSafeImage_urlDoBucketComLixoDepoisDoUuid_eRecusada() {
        String url = BASE + "16cdde0e-6bc4-451c-8b23-def5094c3152?x=<script>";

        assertFalse(criarValidador().isSafeImage(url));
    }

    @Test
    void isSafeImage_lixoAntesDaUrlDoBucket_eRecusada() {
        String url = "https://exemplo.com/redirect?para=" + BASE
                + "16cdde0e-6bc4-451c-8b23-def5094c3152";

        assertFalse(criarValidador().isSafeImage(url));
    }

    @Test
    void isSafeImage_pontoDoDominioEhLiteral() {
        // o "." de oraclecloud.com trocado por "X": só passa se o ponto virar curinga de regex
        String url = "https://objectstorage.sa-saopaulo-1.oraclecloudXcom/n/ns-teste/b/bucket-teste/o/"
                + "16cdde0e-6bc4-451c-8b23-def5094c3152";

        assertFalse(criarValidador().isSafeImage(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",                                          // sem nome de objeto
            "abc",                                       // não é UUID
            "16cdde0e-6bc4-451c-8b23-def5094c315",       // UUID com um caractere a menos
            "16CDDE0E-6BC4-451C-8B23-DEF5094C3152",      // maiúsculas: não é o que emitimos
            "16cdde0e-6bc4-451c-8b23-def5094c3152/",     // barra a mais no fim
            "../16cdde0e-6bc4-451c-8b23-def5094c3152"    // tentativa de sair do caminho
    })
    void isSafeImage_nomeDeObjetoForaDoFormato_eRecusado(String sufixo) {
        assertFalse(criarValidador().isSafeImage(BASE + sufixo));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void isSafeImage_semImagem_ehValido(String url) {
        assertTrue(criarValidador().isSafeImage(url));
    }

    @Test
    void isSafeImage_dataUriDeImagemPermitida_continuaAceitaNaTransicao() {
        assertTrue(criarValidador().isSafeImage("data:image/png;base64,iVBORw0KGgo="));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=",
            "data:image/bmp;base64,Qk0=",
            "data:text/html;base64,PGgxPmE8L2gxPg=="
    })
    void isSafeImage_dataUriDeFormatoNaoPermitido_ehInvalido(String url) {
        assertFalse(criarValidador().isSafeImage(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "data:image/jpeg;base64,/9j/4AAQ",
            "data:image/jpg;base64,/9j/4AAQ",
            "data:image/webp;base64,UklGRg==",
            "data:image/gif;base64,R0lGODlh"
    })
    void isSafeImage_dataUriDeFormatoPermitido_ehValido(String url) {
        assertTrue(criarValidador().isSafeImage(url));
    }
}
