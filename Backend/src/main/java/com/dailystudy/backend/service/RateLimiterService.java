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

    public Bucket resolveLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, key -> criarBuckeLogin());
    }

    public Bucket resolveRegistroBucket(String ip) {
        return registroBuckets.computeIfAbsent(ip, key -> criarBucketRegistro());
    }

    // 5 tentativas de login por minuto, por IP.
    // refillGreedy reabastece os tokens aos poucos (não tudo de uma vez
    // no fim do minuto), evitando picos logo após o reset.
    private Bucket criarBuckeLogin() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(5)))
                .build();
    }

    private Bucket criarBucketRegistro() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(2).refillGreedy(2, Duration.ofMinutes(30)))
                .build();
    }
}
