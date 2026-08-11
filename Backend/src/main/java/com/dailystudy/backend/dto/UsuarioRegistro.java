package com.dailystudy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRegistro {

    @NotBlank
    private String username;

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
    private String email;

    @Size(min = 8)
    @NotBlank()
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=.,<>?])(?=\\S+$).{8,}$",
                message = "A senha deve conter letras maiúsculas, minúsculas, números e caracteres especiais")
    private String senha;
}
