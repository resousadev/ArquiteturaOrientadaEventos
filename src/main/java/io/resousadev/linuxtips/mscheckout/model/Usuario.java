package io.resousadev.linuxtips.mscheckout.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios")
public class Usuario {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 20, message = "Login deve ter entre 3 e 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Column(nullable = false, length = 300)
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles")
    private List<String> roles;

}
