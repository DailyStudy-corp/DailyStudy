package com.dailystudy.backend.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registroBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registroTentativasBuckets = new ConcurrentHashMap<>();

    public Bucket resolveLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, key -> criarBuckeLogin());
    }

    public Bucket resolveRegistroBucket(String ip) {
        return registroBuckets.computeIfAbsent(ip, key -> criarBucketRegistro());
    }

    public Bucket resolveRegistroTentativasBucket(String ip) {
        return registroTentativasBuckets.computeIfAbsent(ip, key -> criarBucketRegistroTentativas());
    }

    // 5 tentativas de login por IP, renovando 5 a cada 5 minutos.
    private Bucket criarBuckeLogin() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(5)))
                .build();
    }

    // Bucket ESTRITO: só conta CONTA CRIADA COM SUCESSO.
    // Erro de validação (400) ou conflito (409) devolve o token no afterCompletion.
    private Bucket criarBucketRegistro() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(2).refillGreedy(2, Duration.ofMinutes(30)))
                .build();
    }

    private Bucket criarBucketRegistroTentativas() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, Duration.ofMinutes(10)))
                .build();
    }
}
