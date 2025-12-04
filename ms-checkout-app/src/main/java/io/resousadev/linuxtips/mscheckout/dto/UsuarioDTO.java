package io.resousadev.linuxtips.mscheckout.dto;

/**
 * DTO para transferência de dados de usuário.
 *
 * @param login identificador de login do usuário
 * @param senha senha do usuário
 * @param roles papéis/permissões do usuário
 */
public record UsuarioDTO(String login, String senha, String[] roles) {

}
