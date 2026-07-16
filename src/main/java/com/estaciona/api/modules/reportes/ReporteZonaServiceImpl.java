package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.reportes.dto.ZonaDisponibilidadProjection;
import com.estaciona.api.modules.reportes.excel.ReporteZonaExcelBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import jakarta.persistence.EntityManager;

/**
 * Servicio para el reporte de disponibilidad de zonas (HU-019).
 */
@Service
public class ReporteZonaServiceImpl {

    private final EntityManager entityManager;

    public ReporteZonaServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Obtiene la lista de todas las zonas habilitadas con datos de disponibilidad.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ZonaDisponibilidadProjection> obtenerDisponibilidad() {
        // JPQL nativa en EntityManager para evitar dependencia circular de repositorios
        var resultList = entityManager.createQuery(
                "SELECT z.id, c.nombre, z.nombre, z.ubicacion, z.tipo, " +
                "z.aforoMaximo, z.aforoDisponible, (z.aforoMaximo - z.aforoDisponible), z.estado " +
                "FROM Zona z JOIN z.campus c " +
                "WHERE z.enabled = true " +
                "ORDER BY c.nombre ASC, z.nombre ASC"
        ).getResultList();

        return resultList.stream().map(row -> {
            Object[] cols = (Object[]) row;
            int aforoMaximo = cols[5] != null ? (Integer) cols[5] : 0;
            int aforoDisponible = cols[6] != null ? (Integer) cols[6] : 0;
            int aforoOcupado = cols[7] != null ? (Integer) cols[7] : 0;
            BigDecimal porcentaje = aforoMaximo > 0
                    ? BigDecimal.valueOf((double) aforoOcupado / aforoMaximo * 100).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new ZonaDisponibilidadProjection() {
                @Override public Integer getId() { return (Integer) cols[0]; }
                @Override public String getCampusNombre() { return (String) cols[1]; }
                @Override public String getZonaNombre() { return (String) cols[2]; }
                @Override public String getUbicacion() { return (String) cols[3]; }
                @Override public String getTipo() { return (String) cols[4]; }
                @Override public Integer getAforoMaximo() { return aforoMaximo; }
                @Override public Integer getAforoDisponible() { return aforoDisponible; }
                @Override public Integer getAforoOcupado() { return aforoOcupado; }
                @Override public BigDecimal getPorcentajeOcupacion() { return porcentaje; }
                @Override public String getEstado() { return (String) cols[8]; }
            };
        }).toList();
    }

    /**
     * Exporta el reporte de disponibilidad de zonas como archivo Excel.
     */
    public byte[] exportarExcel() {
        List<ZonaDisponibilidadProjection> datos = obtenerDisponibilidad();
        return new ReporteZonaExcelBuilder()
                .conTitulo("Reporte de Disponibilidad de Zonas")
                .conDatos(datos)
                .construir();
    }
}
