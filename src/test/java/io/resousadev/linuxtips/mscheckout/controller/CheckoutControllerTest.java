package io.resousadev.linuxtips.mscheckout.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.resousadev.linuxtips.mscheckout.config.SecurityConfiguration;
import io.resousadev.linuxtips.mscheckout.model.Payment;
import io.resousadev.linuxtips.mscheckout.producer.EventBridgeProducer;
import io.resousadev.linuxtips.mscheckout.service.UsuarioService;

/**
 * Web layer tests for {@link CheckoutController}.
 * Uses MockMvc with mocked dependencies.
 */
@WebMvcTest(CheckoutController.class)
@Import(SecurityConfiguration.class)
@DisplayName("CheckoutController Web Tests")
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventBridgeProducer eventBridgeProducer;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/v1/mscheckout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "origem": "test-service",
                        "valor": "100.00",
                        "status": "APPROVED"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Should finish order successfully when authenticated")
    void shouldFinishOrderSuccessfully() throws Exception {
        mockMvc.perform(post("/v1/mscheckout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "origem": "checkout-service",
                        "valor": "150.00",
                        "status": "APPROVED"
                    }
                    """))
            .andExpect(status().isOk());

        verify(eventBridgeProducer).finishOrder(any(Payment.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should accept order with pending status")
    void shouldAcceptOrderWithPendingStatus() throws Exception {
        mockMvc.perform(post("/v1/mscheckout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "origem": "payment-gateway",
                        "valor": "500.00",
                        "status": "PENDING"
                    }
                    """))
            .andExpect(status().isOk());

        verify(eventBridgeProducer).finishOrder(any(Payment.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should accept order with rejected status")
    void shouldAcceptOrderWithRejectedStatus() throws Exception {
        mockMvc.perform(post("/v1/mscheckout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "origem": "fraud-detection",
                        "valor": "10000.00",
                        "status": "REJECTED"
                    }
                    """))
            .andExpect(status().isOk());

        verify(eventBridgeProducer).finishOrder(any(Payment.class));
    }
}
