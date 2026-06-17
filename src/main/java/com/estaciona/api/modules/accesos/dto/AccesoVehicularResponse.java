package com.estaciona.api.modules.accesos.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un acceso vehicular registrado.
 */
public record AccesoVehicularResponse(
        UUID id,
        String placa,
        String marcaModelo,
        String propietario,
        String zona,
        String guardiaEntrada,
        String guardiaSalida,
        OffsetDateTime horaIngreso,
        OffsetDateTime horaSalida,
        String estado
) {}
