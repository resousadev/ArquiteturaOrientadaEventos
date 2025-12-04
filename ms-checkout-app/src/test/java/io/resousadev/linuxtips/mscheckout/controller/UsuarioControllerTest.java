package io.resousadev.linuxtips.mscheckout.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import io.resousadev.linuxtips.mscheckout.dto.UsuarioDTO;
import io.resousadev.linuxtips.mscheckout.mappers.UsuarioMapper;
import io.resousadev.linuxtips.mscheckout.model.Usuario;
import io.resousadev.linuxtips.mscheckout.service.UsuarioService;

/**
 * Web layer tests for {@link UsuarioController}.
 * Uses MockMvc with mocked dependencies.
 */
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfiguration.class)
@DisplayName("UsuarioController Web Tests")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

    @Test
    @DisplayName("Should create user without authentication (public endpoint)")
    void shouldCreateUserWithoutAuthentication() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setLogin("newuser");
        usuario.setSenha("password123");
        usuario.setRoles(List.of("USER"));
        
        when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(usuario);

        // When / Then
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "newuser",
                        "senha": "password123",
                        "roles": ["USER"]
                    }
                    """))
            .andExpect(status().isCreated());

        verify(usuarioMapper).toEntity(any(UsuarioDTO.class));
        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should create user when authenticated as admin")
    void shouldCreateUserWhenAuthenticatedAsAdmin() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setLogin("newuser");
        usuario.setSenha("password123");
        usuario.setRoles(List.of("USER"));
        
        when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(usuario);

        // When / Then
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "newuser",
                        "senha": "password123",
                        "roles": ["USER"]
                    }
                    """))
            .andExpect(status().isCreated());

        verify(usuarioMapper).toEntity(any(UsuarioDTO.class));
        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should create user with multiple roles")
    void shouldCreateUserWithMultipleRoles() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setLogin("adminuser");
        usuario.setSenha("adminPass");
        usuario.setRoles(List.of("USER", "ADMIN"));
        
        when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(usuario);

        // When / Then
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "adminuser",
                        "senha": "adminPass",
                        "roles": ["USER", "ADMIN"]
                    }
                    """))
            .andExpect(status().isCreated());

        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should call mapper to convert DTO to entity")
    void shouldCallMapperToConvertDtoToEntity() throws Exception {
        // Given
        Usuario usuario = new Usuario();
        usuario.setLogin("mapped");
        usuario.setSenha("mappedPass");
        usuario.setRoles(List.of("USER"));
        
        when(usuarioMapper.toEntity(any(UsuarioDTO.class))).thenReturn(usuario);

        // When
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "mapped",
                        "senha": "mappedPass",
                        "roles": ["USER"]
                    }
                    """))
            .andExpect(status().isCreated());

        // Then
        verify(usuarioMapper).toEntity(any(UsuarioDTO.class));
    }
}
