package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.campus.CampusRepository;
import com.estaciona.api.modules.campus.entity.Campus;
import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;
import com.estaciona.api.modules.zonas.entity.Zona;
import com.estaciona.api.modules.zonas.mapper.ZonaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de zonas de estacionamiento.
 * Patrón: Arrange / Act / Assert.
 */
@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {

    @Mock
    private ZonaRepository zonaRepository;

    @Mock
    private CampusRepository campusRepository;

    @Mock
    private ZonaMapper zonaMapper;

    @InjectMocks
    private ZonaServiceImpl zonaService;

    private Campus campusActivo;
    private ZonaRequest request;
    private Zona zonaBuilt;
    private ZonaResponse zonaResponse;

    @BeforeEach
    void setUp() {
        campusActivo = new Campus();
        campusActivo.setId(1);
        campusActivo.setNombre("Campus Central");
        campusActivo.setEnabled(true);

        request = new ZonaRequest(1, "Zona A", "Edificio Principal", "cubierta", 50);

        zonaBuilt = Zona.builder()
                .campus(campusActivo)
                .nombre("Zona A")
                .ubicacion("Edificio Principal")
                .tipo("cubierta")
                .aforoMaximo(50)
                .aforoDisponible(50)
                .build();

        zonaResponse = new ZonaResponse(
                1, 1, "Campus Central", "Zona A",
                "Edificio Principal", "cubierta", 50, 50, "activa", true, null);
    }

    @Test
    @DisplayName("debe_crear_zona_con_aforo_disponible_igual_a_aforo_maximo")
    void debe_crear_zona_con_aforo_disponible_igual_a_aforo_maximo() {
        // Arrange
        when(campusRepository.findById(1)).thenReturn(Optional.of(campusActivo));
        when(zonaRepository.existsByCampusIdAndNombreIgnoreCase(1, "Zona A")).thenReturn(false);
        when(zonaMapper.toEntity(request, campusActivo)).thenReturn(zonaBuilt);
        when(zonaRepository.save(zonaBuilt)).thenReturn(zonaBuilt);
        when(zonaMapper.toResponse(zonaBuilt)).thenReturn(zonaResponse);

        // Act
        ZonaResponse resultado = zonaService.crearZona(request);

        // Assert
        assertThat(resultado.aforoDisponible()).isEqualTo(resultado.aforoMaximo());
        assertThat(resultado.aforoDisponible()).isEqualTo(50);
        verify(zonaRepository, times(1)).save(zonaBuilt);
    }

    @Test
    @DisplayName("debe_asignar_estado_activa_por_defecto")
    void debe_asignar_estado_activa_por_defecto() {
        // Arrange
        when(campusRepository.findById(1)).thenReturn(Optional.of(campusActivo));
        when(zonaRepository.existsByCampusIdAndNombreIgnoreCase(1, "Zona A")).thenReturn(false);
        when(zonaMapper.toEntity(request, campusActivo)).thenReturn(zonaBuilt);
        when(zonaRepository.save(zonaBuilt)).thenReturn(zonaBuilt);
        when(zonaMapper.toResponse(zonaBuilt)).thenReturn(zonaResponse);

        // Act
        ZonaResponse resultado = zonaService.crearZona(request);

        // Assert — el estado "activa" viene de @Builder.Default en la entidad
        assertThat(resultado.estado()).isEqualTo("activa");
    }

    @Test
    @DisplayName("debe_lanzar_404_si_campus_no_existe")
    void debe_lanzar_404_si_campus_no_existe() {
        // Arrange
        when(campusRepository.findById(999)).thenReturn(Optional.empty());
        ZonaRequest requestConCampusInexistente = new ZonaRequest(999, "Zona B", null, "abierta", 30);

        // Act & Assert
        assertThatThrownBy(() -> zonaService.crearZona(requestConCampusInexistente))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(zonaRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_409_si_nombre_de_zona_ya_existe_en_el_campus")
    void debe_lanzar_409_si_nombre_de_zona_ya_existe_en_el_campus() {
        // Arrange
        when(campusRepository.findById(1)).thenReturn(Optional.of(campusActivo));
        when(zonaRepository.existsByCampusIdAndNombreIgnoreCase(1, "Zona A")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> zonaService.crearZona(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(zonaRepository, never()).save(any());
    }
}
