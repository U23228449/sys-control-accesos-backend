package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Estrategia de exportación concreta para formato CSV.
 */
@Component
public class CsvExportacionStrategy implements ExportacionFormatoStrategy {

    @Override
    public byte[] exportar(List<AccesoVehicularReporteProjection> datos) {
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM so Excel opens it correctly
        sb.append("\uFEFF");
        sb.append("Placa,Tipo Vehiculo,Marca/Modelo,Propietario,Zona,Campus,Guardia Entrada,Guardia Salida,Hora Ingreso,Hora Salida,Estado,Permanencia\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (datos != null) {
            for (AccesoVehicularReporteProjection acceso : datos) {
                sb.append(escapeCsv(acceso.getPlaca())).append(",");
                sb.append(escapeCsv(acceso.getTipoVehiculo())).append(",");
                sb.append(escapeCsv(acceso.getMarcaModelo())).append(",");
                sb.append(escapeCsv(acceso.getPropietario())).append(",");
                sb.append(escapeCsv(acceso.getZonaNombre())).append(",");
                sb.append(escapeCsv(acceso.getCampusNombre())).append(",");
                sb.append(escapeCsv(acceso.getGuardiaEntradaNombre())).append(",");
                sb.append(escapeCsv(acceso.getGuardiaSalidaNombre() != null ? acceso.getGuardiaSalidaNombre() : "")).append(",");
                sb.append(acceso.getHoraIngreso() != null ? dtf.format(acceso.getHoraIngreso()) : "").append(",");
                sb.append(acceso.getHoraSalida() != null ? dtf.format(acceso.getHoraSalida()) : "").append(",");
                sb.append(escapeCsv(acceso.getEstado())).append(",");
                
                String permanencia = "";
                if (acceso.getHoraIngreso() != null && acceso.getHoraSalida() != null) {
                    java.time.Duration duration = java.time.Duration.between(acceso.getHoraIngreso(), acceso.getHoraSalida());
                    long horas = duration.toHours();
                    long minutos = duration.toMinutes() % 60;
                    permanencia = horas + "h " + minutos + "m";
                } else if (acceso.getHoraIngreso() != null) {
                    permanencia = "En curso";
                }
                sb.append(escapeCsv(permanencia)).append("\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getExtensionArchivo() {
        return ".csv";
    }
}
