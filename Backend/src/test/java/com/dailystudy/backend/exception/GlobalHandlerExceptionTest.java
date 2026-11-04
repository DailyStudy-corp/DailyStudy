package com.dailystudy.backend.exception;

import com.dailystudy.backend.dto.ExceptionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalHandlerExceptionTest {

    @Test
    void handleInvalidImage_devolve400ComMensagemDaExcecao() {
        GlobalHandlerException handler = new GlobalHandlerException();

        ResponseEntity<ExceptionDTO> resposta =
                handler.handleInvalidImage(new InvalidImageException("Arquivo vazio"));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Arquivo vazio", resposta.getBody().message());  // você completa
    }

    @Test
    void handleMaxUploadSize_devolve413ComMensagem() {
        GlobalHandlerException handler = new GlobalHandlerException();

        ResponseEntity<ExceptionDTO> resposta =
                handler.handleMaxUploadSize(new MaxUploadSizeExceededException(5L * 1024 * 1024));

        assertEquals(HttpStatus.CONTENT_TOO_LARGE, resposta.getStatusCode());
        assertEquals("Imagem grande demais, tamanho máximo 5MB", resposta.getBody().message());
    }
}
