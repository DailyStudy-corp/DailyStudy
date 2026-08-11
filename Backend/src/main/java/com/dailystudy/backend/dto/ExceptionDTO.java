package com.dailystudy.backend.dto;

import java.time.LocalDateTime;

public record ExceptionDTO(int status, String erro, String message, LocalDateTime timestamp) {
}
