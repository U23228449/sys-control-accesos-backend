package com.estaciona.api.modules.roles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un rol del sistema (ADMINISTRADOR, SEGURIDAD, etc.).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
