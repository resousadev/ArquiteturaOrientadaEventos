package io.resousadev.linuxtips.mscheckout.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.resousadev.linuxtips.mscheckout.model.Usuario;
import io.resousadev.linuxtips.mscheckout.service.UsuarioService;

/**
 * Unit tests for {@link CustomUserDetailsService}.
 * Tests Spring Security user loading mechanism.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setLogin("testuser");
        usuario.setSenha("$2a$10$encodedPassword");
        usuario.setRoles(List.of("USER", "ADMIN"));
    }

    @Test
    @DisplayName("Should load user details when user exists")
    void shouldLoadUserDetailsWhenUserExists() {
        // Given
        when(usuarioService.buscarPorLogin("testuser")).thenReturn(usuario);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(usuarioService.buscarPorLogin("nonexistent")).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("Usuário não encontrado: nonexistent");
    }

    @Test
    @DisplayName("Should handle user with single role")
    void shouldHandleUserWithSingleRole() {
        // Given
        usuario.setRoles(List.of("USER"));
        when(usuarioService.buscarPorLogin("testuser")).thenReturn(usuario);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Should handle user with empty roles")
    void shouldHandleUserWithEmptyRoles() {
        // Given
        usuario.setRoles(List.of());
        when(usuarioService.buscarPorLogin("testuser")).thenReturn(usuario);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should preserve case sensitivity in username lookup")
    void shouldPreserveCaseSensitivityInUsername() {
        // Given
        usuario.setLogin("TestUser");
        when(usuarioService.buscarPorLogin("TestUser")).thenReturn(usuario);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("TestUser");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("TestUser");
    }

    @Test
    @DisplayName("Should handle multiple roles correctly")
    void shouldHandleMultipleRolesCorrectly() {
        // Given
        usuario.setRoles(List.of("USER", "ADMIN", "MANAGER", "VIEWER"));
        when(usuarioService.buscarPorLogin("testuser")).thenReturn(usuario);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails.getAuthorities())
            .hasSize(4)
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_VIEWER");
    }
}
