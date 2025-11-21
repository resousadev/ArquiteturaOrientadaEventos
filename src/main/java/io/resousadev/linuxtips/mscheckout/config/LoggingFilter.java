package io.resousadev.linuxtips.mscheckout.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para injetar Correlation ID (rastreamento) em todas as requisições.
 * 
 * O Correlation ID é propagado através do MDC (Mapped Diagnostic Context) do SLF4J,
 * permitindo que todas as mensagens de log de uma mesma requisição sejam correlacionadas.
 * 
 * Padrões implementados:
 * - Aceita correlation ID vindo do header X-Correlation-Id
 * - Gera um novo UUID se não fornecido
 * - Adiciona ao MDC para aparecer em todos os logs
 * - Limpa o MDC após processamento para evitar memory leaks
 * 
 * @author resousadev
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String REQUEST_METHOD_MDC_KEY = "requestMethod";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // Obter ou gerar Correlation ID
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = generateCorrelationId();
            }
            
            // Adicionar ao MDC (disponível em todos os logs desta thread)
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_URI_MDC_KEY, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, httpRequest.getMethod());
            
            log.debug("Iniciando requisição - Method: {}, URI: {}, CorrelationId: {}", 
                    httpRequest.getMethod(), 
                    httpRequest.getRequestURI(), 
                    correlationId);
            
            // Continuar a cadeia de filtros
            chain.doFilter(request, response);
            
        } finally {
            // CRÍTICO: Limpar o MDC para evitar memory leaks em thread pools
            log.debug("Finalizando requisição - CorrelationId: {}", MDC.get(CORRELATION_ID_MDC_KEY));
            MDC.clear();
        }
    }

    /**
     * Gera um novo Correlation ID usando UUID versão 4 (aleatório).
     * Formato: 8-4-4-4-12 hexadecimal (ex: 550e8400-e29b-41d4-a716-446655440000)
     * 
     * @return String UUID gerado
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
