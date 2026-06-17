package com.estaciona.api.modules.reportes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad JPA inmutable mapeada directamente a la vista de base de datos vw_reporte_accesos_vehiculares.
 */
@Entity
@Table(name = "vw_reporte_accesos_vehiculares")
@Immutable
@Getter
public class AccesoVehicularReporte {

    @Id
    private UUID id;

    private String placa;

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;

    @Column(name = "marca_modelo")
    private String marcaModelo;

    private String propietario;

    @Column(name = "zona_nombre")
    private String zonaNombre;

    @Column(name = "campus_nombre")
    private String campusNombre;

    @Column(name = "guardia_entrada_nombre")
    private String guardiaEntradaNombre;

    @Column(name = "guardia_salida_nombre")
    private String guardiaSalidaNombre;

    @Column(name = "hora_ingreso")
    private OffsetDateTime horaIngreso;

    @Column(name = "hora_salida")
    private OffsetDateTime horaSalida;

    private String estado;

    @Column(name = "zona_id")
    private Integer zonaId;

    @Column(name = "guardia_entrada_id")
    private UUID guardiaEntradaId;

    @Column(name = "tipo_zona")
    private String tipoZona;
}
