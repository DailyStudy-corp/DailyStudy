package com.dailystudy.backend.exception;

public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String mensagem){
        super(mensagem);
    }

    public InvalidImageException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
