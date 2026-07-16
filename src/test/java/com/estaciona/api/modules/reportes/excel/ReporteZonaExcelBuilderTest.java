package com.estaciona.api.modules.reportes.excel;

import com.estaciona.api.modules.reportes.dto.ZonaDisponibilidadProjection;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteZonaExcelBuilderTest {

    @Test
    @DisplayName("debe_generar_excel_de_zonas_con_encabezados_datos_y_totales_correctos")
    void debe_generar_excel_de_zonas_con_encabezados_datos_y_totales_correctos() throws Exception {
        // Arrange
        ZonaDisponibilidadProjection z1 = mock(ZonaDisponibilidadProjection.class);
        when(z1.getCampusNombre()).thenReturn("Campus Norte");
        when(z1.getZonaNombre()).thenReturn("Zona A");
        when(z1.getUbicacion()).thenReturn("Edificio A");
        when(z1.getTipo()).thenReturn("interno");
        when(z1.getAforoMaximo()).thenReturn(20);
        when(z1.getAforoDisponible()).thenReturn(15);
        when(z1.getAforoOcupado()).thenReturn(5);
        when(z1.getPorcentajeOcupacion()).thenReturn(BigDecimal.valueOf(25.0));
        when(z1.getEstado()).thenReturn("activa");

        ZonaDisponibilidadProjection z2 = mock(ZonaDisponibilidadProjection.class);
        when(z2.getCampusNombre()).thenReturn("Campus Norte");
        when(z2.getZonaNombre()).thenReturn("Zona B");
        when(z2.getUbicacion()).thenReturn(null); // Ubicación nula para verificar que no falle
        when(z2.getTipo()).thenReturn("externo");
        when(z2.getAforoMaximo()).thenReturn(10);
        when(z2.getAforoDisponible()).thenReturn(6);
        when(z2.getAforoOcupado()).thenReturn(4);
        when(z2.getPorcentajeOcupacion()).thenReturn(BigDecimal.valueOf(40.0));
        when(z2.getEstado()).thenReturn("activa");

        List<ZonaDisponibilidadProjection> datos = List.of(z1, z2);

        // Act
        byte[] bytes = new ReporteZonaExcelBuilder()
                .conTitulo("Disponibilidad de Zonas Test")
                .conDatos(datos)
                .construir();

        // Assert
        assertThat(bytes).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Zonas");
            assertThat(sheet).isNotNull();

            // Titulo
            Row titleRow = sheet.getRow(0);
            assertThat(titleRow.getCell(0).getStringCellValue()).isEqualTo("Disponibilidad de Zonas Test");

            // Cabeceras
            Row headerRow = sheet.getRow(2);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Campus");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Zona");
            assertThat(headerRow.getCell(4).getStringCellValue()).isEqualTo("Aforo Máximo");
            assertThat(headerRow.getCell(7).getStringCellValue()).isEqualTo("% Ocupación");

            // Fila de datos 1 (index 3)
            Row row1 = sheet.getRow(3);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("Campus Norte");
            assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("Zona A");
            assertThat(row1.getCell(2).getStringCellValue()).isEqualTo("Edificio A");
            assertThat(row1.getCell(3).getStringCellValue()).isEqualTo("interno");
            assertThat(row1.getCell(4).getNumericCellValue()).isEqualTo(20.0);
            assertThat(row1.getCell(5).getNumericCellValue()).isEqualTo(15.0);
            assertThat(row1.getCell(6).getNumericCellValue()).isEqualTo(5.0);
            assertThat(row1.getCell(7).getStringCellValue()).isEqualTo("25.0%");
            assertThat(row1.getCell(8).getStringCellValue()).isEqualTo("activa");

            // Fila de datos 2 (index 4)
            Row row2 = sheet.getRow(4);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("Campus Norte");
            assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("Zona B");
            assertThat(row2.getCell(2).getStringCellValue()).isEmpty();
            assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("externo");
            assertThat(row2.getCell(4).getNumericCellValue()).isEqualTo(10.0);
            assertThat(row2.getCell(5).getNumericCellValue()).isEqualTo(6.0);
            assertThat(row2.getCell(6).getNumericCellValue()).isEqualTo(4.0);
            assertThat(row2.getCell(7).getStringCellValue()).isEqualTo("40.0%");
            assertThat(row2.getCell(8).getStringCellValue()).isEqualTo("activa");

            // Fila de resumen (index 5)
            Row resumenRow = sheet.getRow(5);
            assertThat(resumenRow.getCell(0).getStringCellValue()).isEqualTo("TOTALES");
            assertThat(resumenRow.getCell(4).getNumericCellValue()).isEqualTo(30.0); // 20 + 10
            assertThat(resumenRow.getCell(5).getNumericCellValue()).isEqualTo(21.0); // 15 + 6
            assertThat(resumenRow.getCell(6).getNumericCellValue()).isEqualTo(9.0);  // 30 - 21
        }
    }
}
