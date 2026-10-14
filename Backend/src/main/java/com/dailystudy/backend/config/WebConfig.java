package com.dailystudy.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/usuarios/login", "/api/usuarios/registro");

        // Access log para toda a API — registrado com prioridade mais baixa (executa por
        // último no preHandle) para também capturar o status 429 que o RateLimitInterceptor
        // eventualmente gerar.
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**")
                .order(10);
    }
}
