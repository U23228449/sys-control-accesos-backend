package com.estaciona.api.modules.reportes.excel;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class ReporteExcelBuilderTest {

    @Test
    @DisplayName("debe_generar_workbook_con_encabezados_y_datos_correctos")
    void debe_generar_workbook_con_encabezados_y_datos_correctos() throws Exception {
        // Arrange
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime entrada = ahora.minusHours(2).minusMinutes(15);
        OffsetDateTime salida = ahora;

        AccesoVehicularReporteProjection p1 = mock(AccesoVehicularReporteProjection.class);
        when(p1.getPlaca()).thenReturn("ABC-123");
        when(p1.getTipoVehiculo()).thenReturn("auto");
        when(p1.getMarcaModelo()).thenReturn("Toyota Corolla");
        when(p1.getPropietario()).thenReturn("Juan Perez");
        when(p1.getZonaNombre()).thenReturn("Zona A");
        when(p1.getCampusNombre()).thenReturn("Campus Central");
        when(p1.getGuardiaEntradaNombre()).thenReturn("Guardia 1");
        when(p1.getGuardiaSalidaNombre()).thenReturn("Guardia 2");
        when(p1.getHoraIngreso()).thenReturn(entrada);
        when(p1.getHoraSalida()).thenReturn(salida);
        when(p1.getEstado()).thenReturn("completada");

        AccesoVehicularReporteProjection p2 = mock(AccesoVehicularReporteProjection.class);
        when(p2.getPlaca()).thenReturn("XYZ-789");
        when(p2.getTipoVehiculo()).thenReturn("moto");
        when(p2.getMarcaModelo()).thenReturn("Honda CB190");
        when(p2.getPropietario()).thenReturn("Ana Gomez");
        when(p2.getZonaNombre()).thenReturn("Zona B");
        when(p2.getCampusNombre()).thenReturn("Campus Central");
        when(p2.getGuardiaEntradaNombre()).thenReturn("Guardia 1");
        when(p2.getGuardiaSalidaNombre()).thenReturn(null);
        when(p2.getHoraIngreso()).thenReturn(entrada);
        when(p2.getHoraSalida()).thenReturn(null);
        when(p2.getEstado()).thenReturn("en_curso");

        List<AccesoVehicularReporteProjection> datos = List.of(p1, p2);

        // Act
        byte[] bytes = new ReporteExcelBuilder()
                .conTitulo("Reporte de Prueba")
                .conDatos(datos)
                .construir();

        // Assert
        assertThat(bytes).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Accesos");
            assertThat(sheet).isNotNull();

            // Verificar título
            Row titleRow = sheet.getRow(0);
            assertThat(titleRow.getCell(0).getStringCellValue()).isEqualTo("Reporte de Prueba");

            // Verificar encabezados
            Row headerRow = sheet.getRow(2);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Placa");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Tipo Vehículo");
            assertThat(headerRow.getCell(10).getStringCellValue()).isEqualTo("Estado");
            assertThat(headerRow.getCell(11).getStringCellValue()).isEqualTo("Permanencia");

            // Verificar primer registro (fila index 3) - COMPLETADA
            Row row1 = sheet.getRow(3);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("ABC-123");
            assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("auto");
            assertThat(row1.getCell(10).getStringCellValue()).isEqualTo("completada");
            // Permanencia calculada: 2 horas y 15 minutos -> "2h 15m"
            assertThat(row1.getCell(11).getStringCellValue()).isEqualTo("2h 15m");

            // Verificar segundo registro (fila index 4) - EN CURSO
            Row row2 = sheet.getRow(4);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("XYZ-789");
            assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("moto");
            assertThat(row2.getCell(7).getStringCellValue()).isEmpty(); // guardia salida null
            assertThat(row2.getCell(9).getStringCellValue()).isEmpty(); // hora salida null
            assertThat(row2.getCell(10).getStringCellValue()).isEqualTo("en_curso");
            assertThat(row2.getCell(11).getStringCellValue()).isEqualTo("En curso");
        }
    }
}
