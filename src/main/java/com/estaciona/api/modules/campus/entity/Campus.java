package com.estaciona.api.modules.campus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un campus universitario.
 */
@Entity
@Table(name = "campus")
@Getter
@Setter
@NoArgsConstructor
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
