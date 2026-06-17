package com.estaciona.api.modules.usuarios.entity;

import com.estaciona.api.common.audit.AuditableEntity;
import com.estaciona.api.modules.roles.entity.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa a un usuario del sistema.
 * Extiende AuditableEntity para el campo created_at.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "documento", nullable = false, unique = true, length = 20)
    private String documento;

    /** Solo aplica cuando rol = USUARIO. Valores: alumno, docente, personal_admin. */
    @Column(name = "tipo_usuario", length = 50)
    private String tipoUsuario;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
