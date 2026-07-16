package com.estaciona.api.modules.configuraciones.dto;

/**
 * DTO de respuesta para una configuración del sistema.
 */
public record ConfiguracionResponse(
        Integer id,
        String clave,
        String valor,
        String descripcion,
        boolean enabled
) {}
