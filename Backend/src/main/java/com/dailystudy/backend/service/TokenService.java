package com.dailystudy.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.dailystudy.backend.model.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(String.valueOf(usuario.getId()))
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            log.error("Falha ao gerar token JWT para usuarioId={}", usuario.getId(), exception);
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public Long validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
            return Long.valueOf(subject);
        } catch (JWTVerificationException exception){
            log.warn("Token JWT inválido ou expirado: {}", exception.getMessage());
            return null;
        } catch (NumberFormatException exception) {
            log.warn("Token JWT com subject que não é um ID de usuário");
            return null;
        }
    }

    private Instant gerarDataExpiracao() {
        return Instant.now().plus(Duration.ofHours(2));
    }
}