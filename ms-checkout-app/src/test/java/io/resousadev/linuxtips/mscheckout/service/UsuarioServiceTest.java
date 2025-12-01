package io.resousadev.linuxtips.mscheckout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.resousadev.linuxtips.mscheckout.model.Usuario;
import io.resousadev.linuxtips.mscheckout.repository.UsuarioRepository;

/**
 * Unit tests for {@link UsuarioService}.
 * Tests user management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService Unit Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setLogin("newuser");
        usuario.setSenha("plainPassword123");
        usuario.setRoles(List.of("USER"));
    }

    @Test
    @DisplayName("Should encrypt password before saving user")
    void shouldEncryptPasswordBeforeSaving() {
        // Given
        String encodedPassword = "$2a$10$encodedPasswordHash";
        when(passwordEncoder.encode("plainPassword123")).thenReturn(encodedPassword);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.salvarUsuario(usuario);

        // Then
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario savedUsuario = usuarioCaptor.getValue();
        
        assertThat(savedUsuario.getSenha()).isEqualTo(encodedPassword);
        assertThat(savedUsuario.getLogin()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("Should call password encoder with plain password")
    void shouldCallPasswordEncoderWithPlainPassword() {
        // Given
        String plainPassword = "mySecretPassword";
        usuario.setSenha(plainPassword);
        when(passwordEncoder.encode(plainPassword)).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.salvarUsuario(usuario);

        // Then
        verify(passwordEncoder).encode(plainPassword);
    }

    @Test
    @DisplayName("Should save user to repository")
    void shouldSaveUserToRepository() {
        // Given
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.salvarUsuario(usuario);

        // Then
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Should find user by login")
    void shouldFindUserByLogin() {
        // Given
        usuario.setId(UUID.randomUUID());
        when(usuarioRepository.findByLogin("existinguser")).thenReturn(usuario);

        // When
        Usuario found = usuarioService.buscarPorLogin("existinguser");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getLogin()).isEqualTo("newuser");
        verify(usuarioRepository).findByLogin("existinguser");
    }

    @Test
    @DisplayName("Should return null when user not found by login")
    void shouldReturnNullWhenUserNotFoundByLogin() {
        // Given
        when(usuarioRepository.findByLogin("nonexistent")).thenReturn(null);

        // When
        Usuario found = usuarioService.buscarPorLogin("nonexistent");

        // Then
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Should preserve user data except password when saving")
    void shouldPreserveUserDataExceptPasswordWhenSaving() {
        // Given
        UUID userId = UUID.randomUUID();
        usuario.setId(userId);
        usuario.setRoles(List.of("USER", "ADMIN"));
        
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.salvarUsuario(usuario);

        // Then
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario savedUsuario = usuarioCaptor.getValue();
        
        assertThat(savedUsuario.getId()).isEqualTo(userId);
        assertThat(savedUsuario.getLogin()).isEqualTo("newuser");
        assertThat(savedUsuario.getRoles()).containsExactly("USER", "ADMIN");
    }

    @Test
    @DisplayName("Should handle different password formats")
    void shouldHandleDifferentPasswordFormats() {
        // Given
        String complexPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
        usuario.setSenha(complexPassword);
        when(passwordEncoder.encode(complexPassword)).thenReturn("complexEncoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.salvarUsuario(usuario);

        // Then
        verify(passwordEncoder).encode(complexPassword);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getSenha()).isEqualTo("complexEncoded");
    }
}
