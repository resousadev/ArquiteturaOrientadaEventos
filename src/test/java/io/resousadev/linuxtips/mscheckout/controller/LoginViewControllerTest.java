package io.resousadev.linuxtips.mscheckout.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.resousadev.linuxtips.mscheckout.config.SecurityConfiguration;
import io.resousadev.linuxtips.mscheckout.service.UsuarioService;

/**
 * Web layer tests for {@link LoginViewController}.
 * Tests view controller for login and home pages.
 */
@WebMvcTest(LoginViewController.class)
@Import(SecurityConfiguration.class)
@DisplayName("LoginViewController Web Tests")
class LoginViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Should return login page without authentication")
    void shouldReturnLoginPageWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return login view when authenticated")
    void shouldReturnLoginViewWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Should require authentication when accessing home without authentication")
    void shouldRequireAuthenticationWhenAccessingHomeWithoutAuthentication() throws Exception {
        // With httpBasic enabled, unauthenticated requests return 401 or 3xx
        // depending on the Accept header and browser detection
        mockMvc.perform(get("/home"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Should return home view when authenticated")
    void shouldReturnHomeViewWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return home view for admin user")
    void shouldReturnHomeViewForAdminUser() throws Exception {
        mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }
}
