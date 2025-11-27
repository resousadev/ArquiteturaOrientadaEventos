package io.resousadev.linuxtips.mscheckout.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.resousadev.linuxtips.mscheckout.model.Usuario;
import io.resousadev.linuxtips.mscheckout.service.UsuarioService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    @Override
    public UserDetails loadUserByUsername(final String login) throws UsernameNotFoundException {
        // Implementação personalizada para carregar detalhes do usuário
        Usuario usuario = usuarioService.buscarPorLogin(login);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + login);
        }

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenha())
                .roles(usuario.getRoles().toArray(new String[0])) // Defina os papéis conforme necessário
                .build();
    }
}
