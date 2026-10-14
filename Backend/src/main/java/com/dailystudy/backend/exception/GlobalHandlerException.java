package com.dailystudy.backend.exception;

import com.dailystudy.backend.dto.ExceptionDTO;
import com.dailystudy.backend.exception.UsuarioException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<ExceptionDTO> handleUsuarioException(UsuarioException ex){

        log.debug("UsuarioException tratada: {}", ex.getMessage());

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.CONFLICT.value(), "Conflito de dados", ex.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionDTO> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {

        log.debug("Tentativa de login rejeitada: {}", ex.getMessage());

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.UNAUTHORIZED.value(), "Falha de autenticação", ex.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<ExceptionDTO> handlePermissaoNegada(PermissaoNegadaException ex){
        log.debug("Permissao negada: {}", ex.getMessage());

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.FORBIDDEN.value(), "Permissao negada", ex.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleValidationException(MethodArgumentNotValidException ex){

        String mensagemErro = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Erro dos campos");

        log.debug("Validacao de request falhou: {}", mensagemErro);

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), "Registro falhou", mensagemErro, LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGenericException(Exception ex, HttpServletRequest request) {

        //Agora ao inves do erro ser descartado ele vira um log
        log.error("Erro não tratado em {}, {}", request.getMethod(), request.getRequestURI(), ex);

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro do servidor", "Tente mais tarde", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ExceptionDTO> handleRateLimitException(RateLimitException ex) {

        log.debug("RateLimitException tratada: {}", ex.getMessage());

        ExceptionDTO erro = new ExceptionDTO(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Limite excedido",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(erro);
    }

    @ExceptionHandler(CursorInvalidoException.class)
    public ResponseEntity<ExceptionDTO> handleCursorInvalidoException(CursorInvalidoException ex) {

        log.debug("CursorInvalidoException tratado: {}", ex.getMessage());

        ExceptionDTO erro = new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), "Erro na página", "Tente mais tarde", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
