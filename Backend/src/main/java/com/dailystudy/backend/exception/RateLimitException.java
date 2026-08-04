package com.dailystudy.backend.exception;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String mensagem) {
        super(mensagem);
    }
}
