package com.dailystudy.backend.config;

import com.dailystudy.backend.exception.RateLimitException;
import com.dailystudy.backend.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    private static final String LOGIN_PATH = "/api/usuarios/login";
    private static final String REGISTRO_PATH = "/api/usuarios/registro";
    private static final String BUCKET_ATTRIBUTE = "rateLimit.bucket";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        if (REGISTRO_PATH.equals(path)) {
            consumirOuBloquear(rateLimiterService.resolveRegistroTentativasBucket(ip), response, ip, path);
        }

        Bucket bucket = switch (path) {
            case LOGIN_PATH -> rateLimiterService.resolveLoginBucket(ip);
            case REGISTRO_PATH -> rateLimiterService.resolveRegistroBucket(ip);
            default -> null;
        };

        if (bucket == null){
            return true;
        }

        ConsumptionProbe probe = consumirOuBloquear(bucket, response, ip, path);

        // Guarda o bucket estrito na request pra poder devolver o token depois,
        // quando a resposta final (400/409/200) já for conhecida.
        request.setAttribute(BUCKET_ATTRIBUTE, bucket);
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Só o bucket estrito do registro devolve token; login não entra aqui.
        if (!REGISTRO_PATH.equals(request.getRequestURI())) {
            return;
        }

        Bucket bucket = (Bucket) request.getAttribute(BUCKET_ATTRIBUTE);
        if (bucket == null) {
            return;
        }

        int status = response.getStatus();

        if (status == HttpStatus.BAD_REQUEST.value() || status == HttpStatus.CONFLICT.value()) {
            bucket.addTokens(1);
            log.debug("Token devolvido ao bucket de registro: ip-{}, status={}", request.getRemoteAddr(), status);
        }
    }

    private ConsumptionProbe consumirOuBloquear(Bucket bucket, HttpServletResponse response, String ip, String path) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()){
            return probe;
        }

        long secondsAwait = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.addHeader("Retry-After", String.valueOf(secondsAwait));

        log.warn("Rate limit atingido: ip={}, path={}, aguardar={}s", ip, path, secondsAwait);

        throw new RateLimitException("Muitas tentativas. Tente novamente em " + secondsAwait + " segundos");
    }
}
