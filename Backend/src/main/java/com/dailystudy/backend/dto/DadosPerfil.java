package com.dailystudy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosPerfil(@NotBlank @Size(max = 20) String username,
                          @Size(max = 60) String cargo,
                          @Size(max = 200) String bio) {
}
