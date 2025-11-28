package io.resousadev.linuxtips.mscheckout.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link LoggingFilter}.
 * Tests correlation ID handling and MDC population.
 */
@DisplayName("LoggingFilter Unit Tests")
class LoggingFilterTest {

    private LoggingFilter loggingFilter;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should use correlation ID from header when provided")
    void shouldUseCorrelationIdFromHeader() throws IOException, ServletException {
        // Given
        String providedCorrelationId = "test-correlation-id-12345";
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn(providedCorrelationId);
        when(httpRequest.getRequestURI()).thenReturn("/api/test");
        when(httpRequest.getMethod()).thenReturn("GET");

        // Capture MDC values during filter chain execution
        final String[] capturedCorrelationId = new String[1];
        final String[] capturedUri = new String[1];
        final String[] capturedMethod = new String[1];
        
        FilterChain capturingChain = (req, res) -> {
            capturedCorrelationId[0] = MDC.get("correlationId");
            capturedUri[0] = MDC.get("requestUri");
            capturedMethod[0] = MDC.get("requestMethod");
        };

        // When
        loggingFilter.doFilter(httpRequest, httpResponse, capturingChain);

        // Then
        assertThat(capturedCorrelationId[0]).isEqualTo(providedCorrelationId);
        assertThat(capturedUri[0]).isEqualTo("/api/test");
        assertThat(capturedMethod[0]).isEqualTo("GET");
    }

    @Test
    @DisplayName("Should generate new correlation ID when header is missing")
    void shouldGenerateCorrelationIdWhenHeaderMissing() throws IOException, ServletException {
        // Given
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn(null);
        when(httpRequest.getRequestURI()).thenReturn("/api/orders");
        when(httpRequest.getMethod()).thenReturn("POST");

        final String[] capturedCorrelationId = new String[1];
        
        FilterChain capturingChain = (req, res) -> {
            capturedCorrelationId[0] = MDC.get("correlationId");
        };

        // When
        loggingFilter.doFilter(httpRequest, httpResponse, capturingChain);

        // Then
        assertThat(capturedCorrelationId[0])
            .isNotNull()
            .isNotEmpty()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Should generate new correlation ID when header is empty")
    void shouldGenerateCorrelationIdWhenHeaderEmpty() throws IOException, ServletException {
        // Given
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn("   ");
        when(httpRequest.getRequestURI()).thenReturn("/api/users");
        when(httpRequest.getMethod()).thenReturn("GET");

        final String[] capturedCorrelationId = new String[1];
        
        FilterChain capturingChain = (req, res) -> {
            capturedCorrelationId[0] = MDC.get("correlationId");
        };

        // When
        loggingFilter.doFilter(httpRequest, httpResponse, capturingChain);

        // Then
        assertThat(capturedCorrelationId[0])
            .isNotNull()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Should clear MDC after request processing")
    void shouldClearMdcAfterProcessing() throws IOException, ServletException {
        // Given
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn("test-id");
        when(httpRequest.getRequestURI()).thenReturn("/api/test");
        when(httpRequest.getMethod()).thenReturn("GET");

        // When
        loggingFilter.doFilter(httpRequest, httpResponse, filterChain);

        // Then - MDC should be cleared after filter execution
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("requestUri")).isNull();
        assertThat(MDC.get("requestMethod")).isNull();
    }

    @Test
    @DisplayName("Should clear MDC even when exception occurs")
    void shouldClearMdcWhenExceptionOccurs() throws IOException, ServletException {
        // Given
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn("test-id");
        when(httpRequest.getRequestURI()).thenReturn("/api/error");
        when(httpRequest.getMethod()).thenReturn("GET");

        FilterChain throwingChain = (req, res) -> {
            throw new ServletException("Test exception");
        };

        // When / Then
        try {
            loggingFilter.doFilter(httpRequest, httpResponse, throwingChain);
        } catch (ServletException e) {
            // Expected
        }

        // MDC should still be cleared
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    @DisplayName("Should pass through non-HTTP requests without MDC setup")
    void shouldPassThroughNonHttpRequests() throws IOException, ServletException {
        // Given
        ServletRequest nonHttpRequest = mock(ServletRequest.class);

        // When
        loggingFilter.doFilter(nonHttpRequest, httpResponse, filterChain);

        // Then
        verify(filterChain).doFilter(nonHttpRequest, httpResponse);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    @DisplayName("Should call filter chain for HTTP requests")
    void shouldCallFilterChainForHttpRequests() throws IOException, ServletException {
        // Given
        when(httpRequest.getHeader("X-Correlation-Id")).thenReturn("chain-test-id");
        when(httpRequest.getRequestURI()).thenReturn("/api/chain");
        when(httpRequest.getMethod()).thenReturn("PUT");

        // When
        loggingFilter.doFilter(httpRequest, httpResponse, filterChain);

        // Then
        verify(filterChain).doFilter(httpRequest, httpResponse);
    }
}
