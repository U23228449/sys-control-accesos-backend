package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvExportacionStrategyTest {

    private final CsvExportacionStrategy strategy = new CsvExportacionStrategy();

    @Test
    @DisplayName("debe_retornar_content_type_y_extension_correctos")
    void debe_retornar_content_type_y_extension_correctos() {
        assertThat(strategy.getContentType()).isEqualTo("text/csv");
        assertThat(strategy.getExtensionArchivo()).isEqualTo(".csv");
    }

    @Test
    @DisplayName("debe_generar_csv_valido_con_cabecera_y_datos_escapados")
    void debe_generar_csv_valido_con_cabecera_y_datos_escapados() {
        // Arrange
        AccesoVehicularReporteProjection proj = mock(AccesoVehicularReporteProjection.class);
        when(proj.getPlaca()).thenReturn("ABC-123");
        when(proj.getTipoVehiculo()).thenReturn("auto");
        when(proj.getMarcaModelo()).thenReturn("Toyota, Yaris"); // Contiene coma, debe ser escapado
        when(proj.getPropietario()).thenReturn("Juan \"Perez\""); // Contiene comillas, debe ser escapado
        when(proj.getZonaNombre()).thenReturn("Zona A");
        when(proj.getCampusNombre()).thenReturn("Campus Central");
        when(proj.getGuardiaEntradaNombre()).thenReturn("Rosa Quispe");
        when(proj.getGuardiaSalidaNombre()).thenReturn("Lucho Diaz");
        
        java.time.OffsetDateTime ingreso = java.time.OffsetDateTime.of(2026, 6, 19, 8, 0, 0, 0, java.time.ZoneOffset.UTC);
        java.time.OffsetDateTime salida = java.time.OffsetDateTime.of(2026, 6, 19, 10, 30, 0, 0, java.time.ZoneOffset.UTC);
        when(proj.getHoraIngreso()).thenReturn(ingreso);
        when(proj.getHoraSalida()).thenReturn(salida);
        when(proj.getEstado()).thenReturn("completada");

        List<AccesoVehicularReporteProjection> datos = List.of(proj);

        // Act
        byte[] resultBytes = strategy.exportar(datos);
        String result = new String(resultBytes, StandardCharsets.UTF_8);

        // Assert
        assertThat(result).startsWith("\uFEFF"); // UTF-8 BOM
        assertThat(result).contains("Placa,Tipo Vehiculo,Marca/Modelo,Propietario,Zona,Campus,Guardia Entrada,Guardia Salida,Hora Ingreso,Hora Salida,Estado,Permanencia");
        
        // Debe contener los datos escapados correctamente
        assertThat(result).contains("ABC-123");
        assertThat(result).contains("\"Toyota, Yaris\"");
        assertThat(result).contains("\"Juan \"\"Perez\"\"\"");
        assertThat(result).contains("2026-06-19 08:00:00");
        assertThat(result).contains("2026-06-19 10:30:00");
        assertThat(result).contains("2h 30m"); // Permanencia calculada
    }
}
