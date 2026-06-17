package com.estaciona.api.modules.auditoria.entity;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que representa un log de auditoría en el sistema.
 * Es inmutable (append-only).
 */
@Entity
@Table(name = "logs_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // null si fue un trigger automático

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Column(name = "registro_id", nullable = false, length = 36)
    private String registroId;

    @Column(name = "accion", nullable = false, length = 10)
    private String accion; // INSERT, UPDATE, DELETE

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_anteriores", columnDefinition = "jsonb")
    private String valoresAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_nuevos", columnDefinition = "jsonb")
    private String valoresNuevos;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha;
}
