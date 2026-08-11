package com.dailystudy.backend.exception;

import com.dailystudy.backend.dto.ExceptionDTO;
import com.dailystudy.backend.exception.UsuarioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<ExceptionDTO> handleUsuarioException(UsuarioException ex){

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.CONFLICT.value(), "Conflito de dados", ex.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleValidationException(MethodArgumentNotValidException ex){

        String mensagemErro = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Erro dos campos");

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), "Registro falhou", mensagemErro, LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGenericException(Exception ex) {

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro do servidor", "Tente mais tarde", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ExceptionDTO> handleRateLimitException(RateLimitException ex) {

        ExceptionDTO erro = new ExceptionDTO(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Limite excedido",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(erro);
    }
}
