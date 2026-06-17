package com.estaciona.api.modules.accesos.entity;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que representa un registro de acceso vehicular en el sistema.
 */
@Entity
@Table(name = "accesos_vehiculares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccesoVehicular {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // propietario del vehículo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardia_entrada_id", nullable = false)
    private Usuario guardiaEntrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardia_salida_id")
    private Usuario guardiaSalida;

    @Column(name = "hora_ingreso", nullable = false)
    private OffsetDateTime horaIngreso;

    @Column(name = "hora_salida")
    private OffsetDateTime horaSalida;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // 'en_curso', 'completada'

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
