package com.estaciona.api.modules.zonas.entity;

import com.estaciona.api.common.audit.AuditableEntity;
import com.estaciona.api.modules.campus.entity.Campus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una zona de estacionamiento dentro de un campus.
 * Extiende AuditableEntity para el campo created_at.
 * Usa @Builder de Lombok para su construcción (patrón Builder — HU-010).
 */
@Entity
@Table(name = "zonas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zona extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ubicacion", length = 150)
    private String ubicacion;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "aforo_maximo", nullable = false)
    private Integer aforoMaximo;

    /** Se inicializa igual que aforoMaximo al crear la zona. Lo actualiza el trigger de accesos. */
    @Column(name = "aforo_disponible", nullable = false)
    private Integer aforoDisponible;

    /** activa = operativa, cerrada = sin acceso. */
    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "activa";

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
