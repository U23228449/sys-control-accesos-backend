package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponseDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoDesvinculadoResponseDTO;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de vehículos.
 */
@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VehiculoFactory vehiculoFactory;

    @Mock
    private com.estaciona.api.modules.accesos.AccesoVehicularRepository accesoRepository;

    @Mock
    private List<com.estaciona.api.modules.vehiculos.eliminacion.VehiculoEliminacionValidationStrategy> eliminacionStrategies;

    @Mock
    private List<com.estaciona.api.modules.vehiculos.strategy.VehiculoUpdateValidationStrategy> updateStrategies;

    private VehiculoServiceImpl vehiculoService;

    private Usuario propietario;
    private VehiculoRequest requestAuto;
    private VehiculoRequest requestMoto;
    private Vehiculo vehiculoBuilt;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setId(4);
        rol.setNombre("USUARIO");

        propietario = new Usuario();
        propietario.setId(usuarioId);
        propietario.setNombreCompleto("Juan Pérez");
        propietario.setCorreo("juan@test.com");
        propietario.setRol(rol);
        propietario.setEnabled(true);

        requestAuto = new VehiculoRequest("auto", "ABC123", "Toyota Corolla", "Blanco");
        requestMoto = new VehiculoRequest("moto", "XYZ789", "Honda PCX", "Negro");

        vehiculoBuilt = Vehiculo.builder()
                .id(UUID.randomUUID())
                .usuario(propietario)
                .tipo("auto")
                .placa("ABC123")
                .marcaModelo("Toyota Corolla")
                .color("Blanco")
                .build();

        vehiculoService = new VehiculoServiceImpl(
                vehiculoRepository,
                usuarioRepository,
                vehiculoFactory,
                accesoRepository,
                eliminacionStrategies,
                updateStrategies
        );
    }

    @Test
    @DisplayName("debe_registrar_vehiculo_asociado_al_usuario_autenticado")
    void debe_registrar_vehiculo_asociado_al_usuario_autenticado() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(propietario));
        when(vehiculoRepository.existsByPlacaIgnoreCase("ABC123")).thenReturn(false);
        when(vehiculoFactory.crear(requestAuto, propietario)).thenReturn(vehiculoBuilt);
        when(vehiculoRepository.save(vehiculoBuilt)).thenReturn(vehiculoBuilt);

        // Act
        VehiculoResponse response = vehiculoService.registrarVehiculo(requestAuto, usuarioId);

        // Assert — el vehículo está asociado al usuario autenticado
        assertThat(response.propietario().id()).isEqualTo(usuarioId);
        assertThat(response.propietario().nombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(response.placa()).isEqualTo("ABC123");
        verify(vehiculoRepository, times(1)).save(vehiculoBuilt);
    }

    @Test
    @DisplayName("debe_lanzar_409_si_la_placa_ya_existe")
    void debe_lanzar_409_si_la_placa_ya_existe() {
        // Arrange — placa ya registrada por otro vehículo
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(propietario));
        when(vehiculoRepository.existsByPlacaIgnoreCase("ABC123")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> vehiculoService.registrarVehiculo(requestAuto, usuarioId))
                .isInstanceOf(DuplicateResourceException.class);

        verify(vehiculoRepository, never()).save(any());
        verify(vehiculoFactory, never()).crear(any(), any());
    }

    @Test
    @DisplayName("debe_normalizar_placa_a_mayusculas_sin_espacios")
    void debe_normalizar_placa_a_mayusculas_sin_espacios() {
        // Arrange — placa enviada en minúsculas (el service normaliza antes del check)
        var requestConPlacaMinusculas = new VehiculoRequest("auto", "abc 123", "Toyota", "Rojo");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(propietario));
        when(vehiculoRepository.existsByPlacaIgnoreCase("ABC123")).thenReturn(false);
        when(vehiculoFactory.crear(requestConPlacaMinusculas, propietario)).thenReturn(vehiculoBuilt);
        when(vehiculoRepository.save(vehiculoBuilt)).thenReturn(vehiculoBuilt);

        // Act
        vehiculoService.registrarVehiculo(requestConPlacaMinusculas, usuarioId);

        // Assert — el check de unicidad se hace con la placa normalizada
        verify(vehiculoRepository).existsByPlacaIgnoreCase("ABC123");
    }

    @Test
    @DisplayName("debe_aceptar_tipo_auto_y_tipo_moto")
    void debe_aceptar_tipo_auto_y_tipo_moto() {
        // Arrange — tipo moto
        Vehiculo vehiculoMoto = Vehiculo.builder()
                .id(UUID.randomUUID()).usuario(propietario)
                .tipo("moto").placa("XYZ789").marcaModelo("Honda PCX").color("Negro")
                .build();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(propietario));
        when(vehiculoRepository.existsByPlacaIgnoreCase(anyString())).thenReturn(false);
        when(vehiculoFactory.crear(requestMoto, propietario)).thenReturn(vehiculoMoto);
        when(vehiculoRepository.save(vehiculoMoto)).thenReturn(vehiculoMoto);

        // Act
        VehiculoResponse response = vehiculoService.registrarVehiculo(requestMoto, usuarioId);

        // Assert
        assertThat(response.tipo()).isEqualTo("moto");
    }
    @Test
    @DisplayName("debe_retornar_lista_de_vehiculos_del_usuario_autenticado")
    void debe_retornar_lista_de_vehiculos_del_usuario_autenticado() {
        // Arrange
        var p1 = mock(com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection.class);
        when(vehiculoRepository.findAllResumenByUsuarioId(usuarioId)).thenReturn(java.util.List.of(p1));

        // Act
        java.util.List<com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection> result = 
                vehiculoService.consultarMisVehiculos(usuarioId);

        // Assert
        assertThat(result).hasSize(1);
        verify(vehiculoRepository, times(1)).findAllResumenByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("debe_retornar_lista_vacia_si_usuario_no_tiene_vehiculos_sin_lanzar_excepcion")
    void debe_retornar_lista_vacia_si_usuario_no_tiene_vehiculos_sin_lanzar_excepcion() {
        // Arrange
        when(vehiculoRepository.findAllResumenByUsuarioId(usuarioId)).thenReturn(java.util.List.of());

        // Act
        java.util.List<com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection> result = 
                vehiculoService.consultarMisVehiculos(usuarioId);

        // Assert
        assertThat(result).isEmpty();
        verify(vehiculoRepository, times(1)).findAllResumenByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("debe_retornar_vehiculo_al_buscar_por_placa_existente")
    void debe_retornar_vehiculo_al_buscar_por_placa_existente() {
        // Arrange
        var p1 = mock(com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection.class);
        when(vehiculoRepository.findBuscadoByPlacaIgnoreCase("ABC123")).thenReturn(Optional.of(p1));

        // Act
        com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection result = 
                vehiculoService.buscarPorPlaca("ABC123");

        // Assert
        assertThat(result).isNotNull();
        verify(vehiculoRepository, times(1)).findBuscadoByPlacaIgnoreCase("ABC123");
    }

    @Test
    @DisplayName("debe_lanzar_404_al_buscar_por_placa_inexistente")
    void debe_lanzar_404_al_buscar_por_placa_inexistente() {
        // Arrange
        when(vehiculoRepository.findBuscadoByPlacaIgnoreCase("ABC123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehiculoService.buscarPorPlaca("ABC123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehículo no encontrado con placa: ABC123");
    }

    @Test
    @DisplayName("debe_actualizar_vehiculo_exitosamente")
    void debe_actualizar_vehiculo_exitosamente() {
        // Arrange
        UUID vehiculoId = vehiculoBuilt.getId();
        VehiculoUpdateRequestDTO updateRequest = new VehiculoUpdateRequestDTO("auto", "Toyota Corolla Modificado", "Rojo");

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculoBuilt));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        java.util.Iterator<com.estaciona.api.modules.vehiculos.eliminacion.VehiculoEliminacionValidationStrategy> mockIterator = mock(java.util.Iterator.class);
        when(mockIterator.hasNext()).thenReturn(false);
        when(eliminacionStrategies.iterator()).thenReturn(mockIterator);

        java.util.Iterator<com.estaciona.api.modules.vehiculos.strategy.VehiculoUpdateValidationStrategy> mockUpdateIterator = mock(java.util.Iterator.class);
        when(mockUpdateIterator.hasNext()).thenReturn(false);
        when(updateStrategies.iterator()).thenReturn(mockUpdateIterator);

        // Act
        VehiculoResponseDTO response = vehiculoService.actualizarVehiculo(vehiculoId, usuarioId, updateRequest);

        // Assert
        assertThat(response.marcaModelo()).isEqualTo("Toyota Corolla Modificado");
        assertThat(response.color()).isEqualTo("Rojo");
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("debe_eliminar_vehiculo_exitosamente")
    void debe_eliminar_vehiculo_exitosamente() {
        // Arrange
        UUID vehiculoId = vehiculoBuilt.getId();
        when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculoBuilt));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        java.util.Iterator<com.estaciona.api.modules.vehiculos.eliminacion.VehiculoEliminacionValidationStrategy> mockIterator = mock(java.util.Iterator.class);
        when(mockIterator.hasNext()).thenReturn(false);
        when(eliminacionStrategies.iterator()).thenReturn(mockIterator);

        // Act
        VehiculoDesvinculadoResponseDTO response = vehiculoService.eliminarVehiculo(vehiculoId, usuarioId);

        // Assert
        assertThat(vehiculoBuilt.isEnabled()).isFalse();
        verify(vehiculoRepository, times(1)).save(vehiculoBuilt);
        assertThat(response.mensaje()).contains("desvinculado");
    }
}
