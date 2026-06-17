package com.dailystudy.backend.model;

public enum UsuarioRole {

    ADM("adm"),
    USER("user");

    private final String role;

    UsuarioRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}
