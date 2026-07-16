package com.estaciona.api.modules.accesos;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularFiltroRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularHistorialProjection;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.estaciona.api.modules.accesos.validation.AccesoVehicularValidationStrategy;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.VehiculoRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccesoVehicularServiceTest {

    @Mock
    private AccesoVehicularRepository accesoVehicularRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ZonaRepository zonaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AccesoVehicularValidationStrategy strategy;

    private AccesoVehicularServiceImpl service;

    private Usuario propietario;
    private Usuario guardia;
    private Vehiculo vehiculo;
    private Zona zona;
    private AccesoVehicularRequest requestIngreso;
    private AccesoVehicular accesoBuilt;
    private final UUID guardiaId = UUID.randomUUID();
    private final UUID propietarioId = UUID.randomUUID();
    private final UUID accesoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AccesoVehicularServiceImpl(
                accesoVehicularRepository,
                vehiculoRepository,
                zonaRepository,
                usuarioRepository,
                List.of(strategy)
        );

        Rol rol = new Rol();
        rol.setId(4);
        rol.setNombre("USUARIO");

        propietario = new Usuario();
        propietario.setId(propietarioId);
        propietario.setNombreCompleto("Juan Propietario");
        propietario.setRol(rol);

        guardia = new Usuario();
        guardia.setId(guardiaId);
        guardia.setNombreCompleto("Pedro Guardia");

        vehiculo = Vehiculo.builder()
                .id(UUID.randomUUID())
                .placa("ABC123")
                .usuario(propietario)
                .marcaModelo("Toyota")
                .color("Rojo")
                .enabled(true)
                .build();

        zona = Zona.builder()
                .id(1)
                .nombre("Zona Principal")
                .aforoMaximo(10)
                .aforoDisponible(10)
                .estado("activa")
                .enabled(true)
                .build();

        requestIngreso = new AccesoVehicularRequest("ABC123", 1);

        accesoBuilt = AccesoVehicular.builder()
                .id(accesoId)
                .usuario(propietario)
                .vehiculo(vehiculo)
                .zona(zona)
                .guardiaEntrada(guardia)
                .horaIngreso(OffsetDateTime.now())
                .estado("en_curso")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("debe_registrar_ingreso_y_decrementar_aforo_disponible")
    void debe_registrar_ingreso_y_decrementar_aforo_disponible() {
        // Arrange
        when(vehiculoRepository.findByPlacaIgnoreCase("ABC123")).thenReturn(Optional.of(vehiculo));
        when(zonaRepository.findById(1)).thenReturn(Optional.of(zona));
        when(usuarioRepository.findById(guardiaId)).thenReturn(Optional.of(guardia));
        when(accesoVehicularRepository.save(any(AccesoVehicular.class))).thenReturn(accesoBuilt);

        // Act
        AccesoVehicularResponse response = service.registrarIngreso(requestIngreso, guardiaId);

        // Assert
        assertThat(response.id()).isEqualTo(accesoId);
        assertThat(response.placa()).isEqualTo("ABC123");
        assertThat(response.propietario()).isEqualTo("Juan Propietario");
        assertThat(response.estado()).isEqualTo("en_curso");
        assertThat(zona.getAforoDisponible()).isEqualTo(9); // decremented

        verify(strategy, times(1)).validar(vehiculo, zona);
        verify(zonaRepository, times(1)).save(zona);
        verify(accesoVehicularRepository, times(1)).save(any(AccesoVehicular.class));
    }

    @Test
    @DisplayName("debe_lanzar_404_si_placa_no_esta_registrada")
    void debe_lanzar_404_si_placa_no_esta_registrada() {
        // Arrange
        when(vehiculoRepository.findByPlacaIgnoreCase("ABC123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.registrarIngreso(requestIngreso, guardiaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Placa no registrada: ABC123");

        verify(accesoVehicularRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_404_si_zona_no_existe")
    void debe_lanzar_404_si_zona_no_existe() {
        // Arrange
        when(vehiculoRepository.findByPlacaIgnoreCase("ABC123")).thenReturn(Optional.of(vehiculo));
        when(zonaRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.registrarIngreso(requestIngreso, guardiaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zona de estacionamiento no encontrado con id: 1");
    }

    @Test
    @DisplayName("debe_registrar_salida_y_marcar_estado_completada_e_incrementar_aforo")
    void debe_registrar_salida_y_marcar_estado_completada_e_incrementar_aforo() {
        // Arrange — zona con aforo disponible 9 (después de ingreso)
        zona.setAforoDisponible(9);
        accesoBuilt.setEstado("en_curso");

        when(accesoVehicularRepository.findById(accesoId)).thenReturn(Optional.of(accesoBuilt));
        when(usuarioRepository.findById(guardiaId)).thenReturn(Optional.of(guardia));
        when(accesoVehicularRepository.save(accesoBuilt)).thenReturn(accesoBuilt);

        // Act
        AccesoVehicularResponse response = service.registrarSalida(accesoId, guardiaId);

        // Assert
        assertThat(response.estado()).isEqualTo("completada");
        assertThat(response.horaSalida()).isNotNull();
        assertThat(response.guardiaSalida()).isEqualTo("Pedro Guardia");
        assertThat(zona.getAforoDisponible()).isEqualTo(10); // incremented back

        verify(zonaRepository, times(1)).save(zona);
        verify(accesoVehicularRepository, times(1)).save(accesoBuilt);
    }

    @Test
    @DisplayName("debe_lanzar_404_si_acceso_no_existe_en_salida")
    void debe_lanzar_404_si_acceso_no_existe_en_salida() {
        // Arrange
        when(accesoVehicularRepository.findById(accesoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.registrarSalida(accesoId, guardiaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Acceso vehicular no encontrado con id: " + accesoId);
    }

    @Test
    @DisplayName("debe_lanzar_409_si_acceso_ya_esta_completado_en_salida")
    void debe_lanzar_409_si_acceso_ya_esta_completado_en_salida() {
        // Arrange
        accesoBuilt.setEstado("completada");
        accesoBuilt.setHoraSalida(OffsetDateTime.now());

        when(accesoVehicularRepository.findById(accesoId)).thenReturn(Optional.of(accesoBuilt));

        // Act & Assert
        assertThatThrownBy(() -> service.registrarSalida(accesoId, guardiaId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El acceso vehicular ya se encuentra completado.");
    }

    @Test
    @DisplayName("debe_consultar_historial_de_accesos_filtrado")
    void debe_consultar_historial_de_accesos_filtrado() {
        // Arrange
        OffsetDateTime ahora = OffsetDateTime.now();
        AccesoVehicularFiltroRequest filtro = new AccesoVehicularFiltroRequest(ahora.minusDays(1), ahora, "ABC123", "en_curso", 1);
        Pageable pageable = PageRequest.of(0, 10);

        when(zonaRepository.existsById(1)).thenReturn(true);

        AccesoVehicular mockAcceso = mock(AccesoVehicular.class);
        Vehiculo mockVehiculo = mock(Vehiculo.class);
        Usuario mockUsuario = mock(Usuario.class);
        Zona mockZona = mock(Zona.class);
        com.estaciona.api.modules.campus.entity.Campus mockCampus = mock(com.estaciona.api.modules.campus.entity.Campus.class);
        Usuario mockGuardia = mock(Usuario.class);

        when(mockAcceso.getId()).thenReturn(UUID.randomUUID());
        when(mockAcceso.getVehiculo()).thenReturn(mockVehiculo);
        when(mockVehiculo.getPlaca()).thenReturn("ABC123");
        when(mockVehiculo.getTipo()).thenReturn("auto");
        when(mockVehiculo.getMarcaModelo()).thenReturn("Toyota");
        when(mockAcceso.getUsuario()).thenReturn(mockUsuario);
        when(mockUsuario.getNombreCompleto()).thenReturn("Juan Pérez");
        when(mockAcceso.getZona()).thenReturn(mockZona);
        when(mockZona.getNombre()).thenReturn("Zona A");
        when(mockZona.getCampus()).thenReturn(mockCampus);
        when(mockCampus.getNombre()).thenReturn("Campus Norte");
        when(mockAcceso.getGuardiaEntrada()).thenReturn(mockGuardia);
        when(mockGuardia.getNombreCompleto()).thenReturn("Guardia Uno");
        when(mockAcceso.getHoraIngreso()).thenReturn(ahora);
        when(mockAcceso.getEstado()).thenReturn("en_curso");

        Page<AccesoVehicular> page = new PageImpl<>(List.of(mockAcceso));
        when(accesoVehicularRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<AccesoVehicularHistorialProjection> result = service.consultarHistorial(filtro, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPlaca()).isEqualTo("ABC123");
        verify(accesoVehicularRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }
}
