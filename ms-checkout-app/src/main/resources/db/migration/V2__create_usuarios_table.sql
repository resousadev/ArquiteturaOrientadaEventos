-- V2__create_usuarios_table.sql
-- Creates the usuarios table and related structures
-- Tables are created in the 'checkout' schema (configured via Flyway default-schema)

-- Main usuarios table
CREATE TABLE usuarios (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    login VARCHAR(20) NOT NULL,
    senha VARCHAR(300) NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_login UNIQUE (login)
);

-- Roles table for @ElementCollection mapping
-- JPA creates this as a join table for List<String> roles
CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL,
    roles VARCHAR(50) NOT NULL,
    CONSTRAINT fk_usuario_roles_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE
);

-- Index for faster role lookups
CREATE INDEX idx_usuario_roles_usuario_id ON usuario_roles(usuario_id);

-- Comments for documentation
COMMENT ON TABLE usuarios IS 'User accounts for authentication';
COMMENT ON COLUMN usuarios.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN usuarios.login IS 'Unique username for authentication';
COMMENT ON COLUMN usuarios.senha IS 'Encrypted password (BCrypt)';
COMMENT ON TABLE usuario_roles IS 'User roles for authorization (ElementCollection)';
