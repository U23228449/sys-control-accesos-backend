package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Estrategia de exportación concreta para formato PDF (.pdf).
 * Implementa ExportacionFormatoStrategy y genera el reporte estructurado usando OpenPDF.
 */
@Component
public class PdfExportacionStrategy implements ExportacionFormatoStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public byte[] exportar(List<AccesoVehicularReporteProjection> datos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 1. Crear documento PDF en formato horizontal (Landscape)
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // 2. Título principal
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(26, 54, 93));
            Paragraph titulo = new Paragraph("Reporte de Accesos Vehiculares", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            document.add(titulo);

            // 3. Metadatos de generación
            Font fontMeta = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
            Paragraph meta = new Paragraph(
                    "Generado el: " + OffsetDateTime.now().format(DATE_FORMATTER) +
                    "  |  Total de registros: " + datos.size(), fontMeta);
            meta.setAlignment(Element.ALIGN_RIGHT);
            meta.setSpacingAfter(15);
            document.add(meta);

            // 4. Crear tabla de datos (10 columnas)
            float[] colWidths = {8f, 7f, 13f, 13f, 10f, 12f, 12f, 14f, 14f, 7f};
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(colWidths);
            table.setHeaderRows(1);

            // Cabeceras de la tabla con color azul oscuro y texto blanco
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            String[] headers = {
                    "Placa", "Tipo", "Marca/Modelo", "Propietario",
                    "Zona", "Guardia Ent.", "Guardia Sal.",
                    "H. Ingreso", "H. Salida", "Estado"
            };

            Color colorHeaderBg = new Color(26, 54, 93);
            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(headerText, fontHeader));
                cell.setBackgroundColor(colorHeaderBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Cuerpo de la tabla
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Color alternateBg = new Color(240, 244, 248);

            boolean isAlternate = false;
            for (AccesoVehicularReporteProjection row : datos) {
                Color currentBg = isAlternate ? alternateBg : Color.WHITE;

                // 1. Placa
                addCell(table, row.getPlaca(), fontBody, currentBg, Element.ALIGN_CENTER);
                // 2. Tipo
                addCell(table, row.getTipoVehiculo() != null ? row.getTipoVehiculo().toUpperCase() : "-", fontBody, currentBg, Element.ALIGN_CENTER);
                // 3. Marca/Modelo
                addCell(table, row.getMarcaModelo(), fontBody, currentBg, Element.ALIGN_LEFT);
                // 4. Propietario
                addCell(table, row.getPropietario(), fontBody, currentBg, Element.ALIGN_LEFT);
                // 5. Zona
                addCell(table, row.getZonaNombre(), fontBody, currentBg, Element.ALIGN_LEFT);
                // 6. Guardia Entrada
                addCell(table, row.getGuardiaEntradaNombre(), fontBody, currentBg, Element.ALIGN_LEFT);
                // 7. Guardia Salida
                addCell(table, row.getGuardiaSalidaNombre() != null ? row.getGuardiaSalidaNombre() : "-", fontBody, currentBg, Element.ALIGN_LEFT);
                // 8. Hora Ingreso
                addCell(table, formatFecha(row.getHoraIngreso()), fontBody, currentBg, Element.ALIGN_CENTER);
                // 9. Hora Salida
                addCell(table, formatFecha(row.getHoraSalida()), fontBody, currentBg, Element.ALIGN_CENTER);
                // 10. Estado
                addCell(table, row.getEstado() != null ? row.getEstado().toUpperCase() : "-", fontBody, currentBg, Element.ALIGN_CENTER);

                isAlternate = !isAlternate;
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte en formato PDF", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatFecha(OffsetDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return fecha.format(DATE_FORMATTER);
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getExtensionArchivo() {
        return ".pdf";
    }
}
