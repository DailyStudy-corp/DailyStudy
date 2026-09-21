package com.dailystudy.backend.config;

import com.dailystudy.backend.exception.RateLimitException;
import com.dailystudy.backend.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        Bucket bucket = switch (path) {
            case "/api/usuarios/login" -> rateLimiterService.resolveLoginBucket(ip);
            case "/api/usuarios/registro" -> rateLimiterService.resolveRegistroBucket(ip);
            default -> null;
        };

        if (bucket == null){
            return true;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long secondsAwait = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.addHeader("Retry-After", String.valueOf(secondsAwait));

        log.warn("Rate limit atingido: ip={}, path={}, aguardar={}s", ip, path, secondsAwait);

        throw new RateLimitException("Muitas tentativas. Tente novamente em " + secondsAwait + "segundos");
    }
}
