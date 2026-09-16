package com.dailystudy.backend.model;

public enum CampoIndexado {
    USERNAME(3),
    CONTEUDO(2),
    CARGO(1),
    BIO(1);

    private final int peso;

    CampoIndexado(int peso){
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
