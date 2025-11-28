package io.resousadev.linuxtips.mscheckout.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.resousadev.linuxtips.mscheckout.model.Usuario;

import java.util.UUID;

/**
 * Repositório JPA para operações de persistência de usuários.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Usuario findByLogin(String login);

}
