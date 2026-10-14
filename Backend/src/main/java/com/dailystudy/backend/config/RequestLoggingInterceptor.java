package com.dailystudy.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Log de acesso (access log) para TODAS as rotas de /api.
 *
 * Em vez de espalhar "log.info(...)" em cada método de cada Controller (repetitivo
 * e fácil de esquecer em endpoint novo), um único HandlerInterceptor intercepta
 * toda requisição HTTP que entra no Spring MVC. Isso cobre o "controllers" pedido
 * no checklist de logging sem duplicar código: nasce um endpoint novo, o log já
 * existe de graça.
 *
 * O nível é escolhido pelo status HTTP da resposta:
 *  - 5xx  -> ERROR (bug real do servidor)
 *  - 4xx  -> WARN  (cliente fez algo inválido/não autorizado)
 *  - resto -> INFO (fluxo normal - é o "access log" clássico)
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private final String ATRIBUTO_INICIO = "requestStartTimeMillis";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATRIBUTO_INICIO, System.currentTimeMillis());
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long duracaoMs = calcularDuracao(request);
        int status = response.getStatus();
        String usuario = usuarioAutenticado();

        if (status >= 500) {
            log.error("{} {} -> {} ({}ms) usuario={}", request.getMethod(), request.getRequestURI(), status, duracaoMs, usuario);
        } else if (status >= 400) {
            log.warn("{} {} -> {} ({}ms) usuario={}", request.getMethod(), request.getRequestURI(), status, duracaoMs, usuario);
        } else {
            log.info("{} {} -> {} ({}ms) usuario={}", request.getMethod(), request.getRequestURI(), status, duracaoMs, usuario);
        }
    }

    private long calcularDuracao(HttpServletRequest request){
        Object inicio = request.getAttribute(ATRIBUTO_INICIO);
        if (!(inicio instanceof Long inicioMillis)) {
            return -1;
        }
        return System.currentTimeMillis() - inicioMillis;
    }

    private String usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        return autenticado ? authentication.getName() : "anonimo";
    }
}
