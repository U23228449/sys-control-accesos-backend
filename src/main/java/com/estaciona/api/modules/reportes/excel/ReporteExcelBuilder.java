package com.estaciona.api.modules.reportes.excel;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Builder para generar archivos Excel (.xlsx) a partir de listados de accesos vehiculares.
 * Diseñado utilizando SXSSFWorkbook para optimizar el consumo de memoria en streaming.
 */
public class ReporteExcelBuilder {

    private String titulo = "Reporte de Accesos Vehiculares";
    private List<AccesoVehicularReporteProjection> datos;

    public ReporteExcelBuilder conTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public ReporteExcelBuilder conDatos(List<AccesoVehicularReporteProjection> datos) {
        this.datos = datos;
        return this;
    }

    /**
     * Construye el libro de Excel y lo retorna como un arreglo de bytes.
     */
    public byte[] construir() {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Accesos");

            // Estilos de encabezado
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo de fecha y hora
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            // Fila de Título (Fila 0)
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Encabezados de Columna (Fila 2)
            String[] headers = {
                    "Placa", "Tipo Vehículo", "Marca/Modelo", "Propietario",
                    "Zona", "Campus", "Guardia Entrada", "Guardia Salida",
                    "Hora Ingreso", "Hora Salida", "Estado", "Permanencia"
            };

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Llenado de Datos (Fila 3 en adelante)
            int rowIdx = 3;
            if (datos != null) {
                for (AccesoVehicularReporteProjection acceso : datos) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(acceso.getPlaca());
                    row.createCell(1).setCellValue(acceso.getTipoVehiculo());
                    row.createCell(2).setCellValue(acceso.getMarcaModelo());
                    row.createCell(3).setCellValue(acceso.getPropietario());
                    row.createCell(4).setCellValue(acceso.getZonaNombre());
                    row.createCell(5).setCellValue(acceso.getCampusNombre());
                    row.createCell(6).setCellValue(acceso.getGuardiaEntradaNombre());
                    row.createCell(7).setCellValue(acceso.getGuardiaSalidaNombre() != null ? acceso.getGuardiaSalidaNombre() : "");

                    Cell ingresoCell = row.createCell(8);
                    if (acceso.getHoraIngreso() != null) {
                        ingresoCell.setCellValue(acceso.getHoraIngreso().toLocalDateTime());
                        ingresoCell.setCellStyle(dateCellStyle);
                    }

                    Cell salidaCell = row.createCell(9);
                    if (acceso.getHoraSalida() != null) {
                        salidaCell.setCellValue(acceso.getHoraSalida().toLocalDateTime());
                        salidaCell.setCellStyle(dateCellStyle);
                    } else {
                        salidaCell.setCellValue("");
                    }

                    row.createCell(10).setCellValue(acceso.getEstado());

                    // Calcular tiempo de permanencia si tiene hora de salida
                    String permanencia = "";
                    if (acceso.getHoraIngreso() != null && acceso.getHoraSalida() != null) {
                        Duration duration = Duration.between(acceso.getHoraIngreso(), acceso.getHoraSalida());
                        long horas = duration.toHours();
                        long minutos = duration.toMinutes() % 60;
                        permanencia = horas + "h " + minutos + "m";
                    } else if (acceso.getHoraIngreso() != null) {
                        permanencia = "En curso";
                    }
                    row.createCell(11).setCellValue(permanencia);
                }
            }

            // Habilitar seguimiento para el auto-ajuste de columnas y ajustar
            sheet.trackAllColumnsForAutoSizing();
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error fatal al construir archivo Excel", e);
        }
    }
}
