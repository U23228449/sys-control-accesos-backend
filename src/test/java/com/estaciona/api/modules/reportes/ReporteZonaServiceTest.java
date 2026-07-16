package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.reportes.dto.ZonaDisponibilidadProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteZonaServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private ReporteZonaServiceImpl reporteZonaService;

    @Test
    @DisplayName("debe_obtener_disponibilidad_de_zonas_correctamente")
    void debe_obtener_disponibilidad_de_zonas_correctamente() {
        // Arrange
        Object[] row1 = new Object[] {
                1,              // id
                "Campus Norte",  // campus nombre
                "Zona A",       // zona nombre
                "Edificio A",   // ubicacion
                "interno",      // tipo
                20,             // aforoMaximo
                15,             // aforoDisponible
                5,              // aforoOcupado
                "activa"        // estado
        };

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of((Object) row1));

        // Act
        List<ZonaDisponibilidadProjection> resultado = reporteZonaService.obtenerDisponibilidad();

        // Assert
        assertThat(resultado).hasSize(1);
        ZonaDisponibilidadProjection p = resultado.get(0);
        assertThat(p.getId()).isEqualTo(1);
        assertThat(p.getCampusNombre()).isEqualTo("Campus Norte");
        assertThat(p.getZonaNombre()).isEqualTo("Zona A");
        assertThat(p.getUbicacion()).isEqualTo("Edificio A");
        assertThat(p.getTipo()).isEqualTo("interno");
        assertThat(p.getAforoMaximo()).isEqualTo(20);
        assertThat(p.getAforoDisponible()).isEqualTo(15);
        assertThat(p.getAforoOcupado()).isEqualTo(5);
        assertThat(p.getPorcentajeOcupacion()).isEqualTo(BigDecimal.valueOf(25.0).setScale(2));
        assertThat(p.getEstado()).isEqualTo("activa");
    }
}
