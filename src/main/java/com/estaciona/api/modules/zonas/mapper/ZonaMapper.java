package com.estaciona.api.modules.zonas.mapper;

import com.estaciona.api.modules.campus.entity.Campus;
import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct entre ZonaRequest/Zona/ZonaResponse.
 * Usa el Builder de Lombok de Zona (detección automática con lombok-mapstruct-binding).
 */
@Mapper(componentModel = "spring")
public interface ZonaMapper {

    /**
     * Construye la entidad Zona via el Builder de Lombok.
     * - aforoDisponible se inicializa igual que aforoMaximo (zona nace llena de espacios).
     * - estado y enabled usan sus @Builder.Default ("activa" y true).
     * - id y createdAt son ignorados (los gestiona la BD/Auditing).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campus", source = "campus")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "ubicacion", source = "request.ubicacion")
    @Mapping(target = "tipo", source = "request.tipo")
    @Mapping(target = "aforoMaximo", source = "request.aforoMaximo")
    @Mapping(target = "aforoDisponible", source = "request.aforoMaximo")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    Zona toEntity(ZonaRequest request, Campus campus);

    /** Aplana el objeto Campus en campusId + campusNombre. */
    @Mapping(target = "campusId", source = "campus.id")
    @Mapping(target = "campusNombre", source = "campus.nombre")
    @Mapping(target = "createdAt", source = "createdAt")
    ZonaResponse toResponse(Zona zona);
}
