package com.dailystudy.backend.util;

import com.dailystudy.backend.exception.CursorInvalidoException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Slf4j
public class CursorCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record CursorData(Instant dataCriacao, String id) {}

    public static String encode(Instant dataCriacao, String id) {
        try {
            String json = MAPPER.writeValueAsString(new CursorData(dataCriacao, id));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JacksonException e) {
            throw new IllegalStateException("Falha ao gerar cursor", e);
        }
    }

    public static CursorData decode(String cursor){
        if (cursor == null || cursor.isBlank()) return null;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return MAPPER.readValue(bytes, CursorData.class);
        } catch (Exception e) {
            log.warn("Cursor inválido recebido: '{}'", cursor, e);
            throw new CursorInvalidoException("Cursor inválido ou expirado");
        }
    }
}
