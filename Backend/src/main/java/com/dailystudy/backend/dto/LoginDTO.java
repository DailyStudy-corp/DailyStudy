package com.dailystudy.backend.dto;

import com.dailystudy.backend.util.Normalizador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @Email(message = "Insira um email válido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    public void setEmail(String email) {
        this.email = Normalizador.normalizarEmail(email);
    }
}
