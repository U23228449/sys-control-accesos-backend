package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExcelExportacionStrategyTest {

    private final ExcelExportacionStrategy strategy = new ExcelExportacionStrategy();

    @Test
    @DisplayName("debe_retornar_content_type_y_extension_correctos")
    void debe_retornar_content_type_y_extension_correctos() {
        assertThat(strategy.getContentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(strategy.getExtensionArchivo()).isEqualTo(".xlsx");
    }

    @Test
    @DisplayName("debe_generar_excel_valido_al_exportar")
    void debe_generar_excel_valido_al_exportar() {
        // Arrange
        AccesoVehicularReporteProjection proj = mock(AccesoVehicularReporteProjection.class);
        List<AccesoVehicularReporteProjection> datos = List.of(proj);

        // Act
        byte[] result = strategy.exportar(datos);

        // Assert
        assertThat(result).isNotEmpty();
    }
}
