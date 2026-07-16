package com.estaciona.api.modules.configuraciones.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA para la tabla configuraciones.
 * Almacena parámetros dinámicos del sistema (JWT, límites, etc.).
 */
@Entity
@Table(name = "configuraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @Column(name = "valor", nullable = false, length = 255)
    private String valor;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
