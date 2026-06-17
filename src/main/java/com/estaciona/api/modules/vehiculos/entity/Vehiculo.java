package com.estaciona.api.modules.vehiculos.entity;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidad que representa un vehículo registrado en el sistema.
 * No extiende AuditableEntity: el esquema no tiene columna created_at para vehiculos.
 * Usa @Builder de Lombok para construcción vía VehiculoFactory.
 */
@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Valores permitidos: 'auto', 'moto'.
     * La constraint CHECK está definida en el esquema PostgreSQL (V1).
     */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    /** Placa única. La constraint UNIQUE es doble defensa (además de existsByPlaca). */
    @Column(name = "placa", nullable = false, unique = true, length = 15)
    private String placa;

    @Column(name = "marca_modelo", nullable = false, length = 100)
    private String marcaModelo;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
