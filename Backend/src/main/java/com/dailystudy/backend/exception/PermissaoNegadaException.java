package com.dailystudy.backend.exception;

// aqui o usuário está autenticado (sabemos quem ele é), só não é o dono do recurso.
// Sem essa exceção, "editar post de outro autor" caía no RuntimeException genérico
// e virava 500 no GlobalHandlerException — exatamente o que você viu no teste.
public class PermissaoNegadaException extends RuntimeException {
    public PermissaoNegadaException(String message) {
        super(message);
    }
}
