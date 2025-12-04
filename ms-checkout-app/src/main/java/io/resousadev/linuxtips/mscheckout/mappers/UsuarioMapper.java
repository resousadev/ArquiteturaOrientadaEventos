package io.resousadev.linuxtips.mscheckout.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.resousadev.linuxtips.mscheckout.dto.UsuarioDTO;
import io.resousadev.linuxtips.mscheckout.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    Usuario toEntity(UsuarioDTO dto);
    
}
