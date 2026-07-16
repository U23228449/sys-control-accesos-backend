package com.estaciona.api.modules.reportes.excel;

import com.estaciona.api.modules.reportes.dto.ZonaDisponibilidadProjection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Builder para generar archivos Excel (.xlsx) con el reporte de disponibilidad de zonas (HU-019).
 * Usa SXSSFWorkbook para streaming eficiente en memoria.
 */
public class ReporteZonaExcelBuilder {

    private String titulo = "Reporte de Zonas de Estacionamiento";
    private List<ZonaDisponibilidadProjection> datos;

    public ReporteZonaExcelBuilder conTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public ReporteZonaExcelBuilder conDatos(List<ZonaDisponibilidadProjection> datos) {
        this.datos = datos;
        return this;
    }

    /**
     * Construye el libro de Excel con reporte de zonas y retorna los bytes.
     */
    public byte[] construir() {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Zonas");

            // Estilo encabezado
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo fila de resumen
            CellStyle resumenStyle = workbook.createCellStyle();
            Font resumenFont = workbook.createFont();
            resumenFont.setBold(true);
            resumenStyle.setFont(resumenFont);
            resumenStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            resumenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Fila de título (fila 0)
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Encabezados de columna (fila 2)
            String[] headers = {
                    "Campus", "Zona", "Ubicación", "Tipo",
                    "Aforo Máximo", "Aforo Disponible", "Aforo Ocupado",
                    "% Ocupación", "Estado"
            };

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos (fila 3 en adelante)
            int rowIdx = 3;
            int totalMaximo = 0;
            int totalDisponible = 0;

            if (datos != null) {
                for (ZonaDisponibilidadProjection zona : datos) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(zona.getCampusNombre());
                    row.createCell(1).setCellValue(zona.getZonaNombre());
                    row.createCell(2).setCellValue(zona.getUbicacion() != null ? zona.getUbicacion() : "");
                    row.createCell(3).setCellValue(zona.getTipo());

                    Cell aforoMax = row.createCell(4);
                    aforoMax.setCellValue(zona.getAforoMaximo() != null ? zona.getAforoMaximo() : 0);

                    Cell aforoDisp = row.createCell(5);
                    aforoDisp.setCellValue(zona.getAforoDisponible() != null ? zona.getAforoDisponible() : 0);

                    Cell aforoOcup = row.createCell(6);
                    aforoOcup.setCellValue(zona.getAforoOcupado() != null ? zona.getAforoOcupado() : 0);

                    Cell porcentaje = row.createCell(7);
                    porcentaje.setCellValue(zona.getPorcentajeOcupacion() != null
                            ? zona.getPorcentajeOcupacion().doubleValue() + "%" : "0%");

                    row.createCell(8).setCellValue(zona.getEstado());

                    // Acumular totales
                    totalMaximo += zona.getAforoMaximo() != null ? zona.getAforoMaximo() : 0;
                    totalDisponible += zona.getAforoDisponible() != null ? zona.getAforoDisponible() : 0;
                }

                // Fila de resumen al final
                Row resumenRow = sheet.createRow(rowIdx);
                resumenRow.createCell(0).setCellValue("TOTALES");
                resumenRow.createCell(4).setCellValue(totalMaximo);
                resumenRow.createCell(5).setCellValue(totalDisponible);
                resumenRow.createCell(6).setCellValue(totalMaximo - totalDisponible);

                // Aplicar estilo a la fila de resumen
                for (int i = 0; i <= 8; i++) {
                    Cell c = resumenRow.getCell(i);
                    if (c == null) c = resumenRow.createCell(i);
                    c.setCellStyle(resumenStyle);
                }
            }

            // Auto-dimensionar columnas
            sheet.trackAllColumnsForAutoSizing();
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error fatal al construir Excel de zonas", e);
        }
    }
}
