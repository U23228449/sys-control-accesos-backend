package com.estaciona.api.modules.reportes;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import com.estaciona.api.modules.reportes.entity.AccesoVehicularReporte;
import com.estaciona.api.modules.reportes.strategy.ExportacionFormatoStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteAccesoVehicularServiceTest {

    @Mock
    private AccesoVehicularReporteRepository repository;

    @Mock
    private ExportacionFormatoStrategy excelStrategy;

    @Mock
    private ExportacionFormatoStrategy pdfStrategy;

    private ReporteAccesoVehicularServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(excelStrategy.getExtensionArchivo()).thenReturn(".xlsx");
        lenient().when(excelStrategy.getContentType()).thenReturn("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        lenient().when(pdfStrategy.getExtensionArchivo()).thenReturn(".pdf");
        lenient().when(pdfStrategy.getContentType()).thenReturn("application/pdf");

        service = new ReporteAccesoVehicularServiceImpl(repository, List.of(excelStrategy, pdfStrategy));
    }

    @Test
    @DisplayName("debe_generar_reporte_paginado_aplicando_filtros")
    @SuppressWarnings("unchecked")
    void debe_generar_reporte_paginado_aplicando_filtros() {
        // Arrange
        var filtro = new ReporteAccesoFiltroRequest(
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                1,
                UUID.randomUUID(),
                "completada",
                "interno",
                "auto"
        );
        Pageable pageable = PageRequest.of(0, 10);
        AccesoVehicularReporteProjection projection = mock(AccesoVehicularReporteProjection.class);
        Page<AccesoVehicularReporteProjection> page = new PageImpl<>(List.of(projection), pageable, 1);

        when(repository.findBy(any(Specification.class), any(Function.class))).thenReturn(page);

        // Act
        Page<AccesoVehicularReporteProjection> result = service.generarReporte(filtro, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    @DisplayName("debe_invocar_estrategia_de_exportacion_excel_al_exportar")
    @SuppressWarnings("unchecked")
    void debe_invocar_estrategia_de_exportacion_excel_al_exportar() {
        // Arrange
        var filtro = new ReporteAccesoFiltroRequest(null, null, null, null, null, null, null);
        AccesoVehicularReporteProjection projection = mock(AccesoVehicularReporteProjection.class);
        List<AccesoVehicularReporteProjection> lista = List.of(projection);
        byte[] bytesMock = new byte[]{1, 2, 3};

        when(repository.findBy(any(Specification.class), any(Function.class))).thenReturn(lista);
        when(excelStrategy.exportar(lista)).thenReturn(bytesMock);

        // Act
        byte[] result = service.exportarReporte(filtro, "excel");

        // Assert
        assertThat(result).isEqualTo(bytesMock);
        verify(repository, times(1)).findBy(any(Specification.class), any(Function.class));
        verify(excelStrategy, times(1)).exportar(lista);
    }

    @Test
    @DisplayName("debe_invocar_estrategia_de_exportacion_pdf_al_exportar")
    @SuppressWarnings("unchecked")
    void debe_invocar_estrategia_de_exportacion_pdf_al_exportar() {
        // Arrange
        var filtro = new ReporteAccesoFiltroRequest(null, null, null, null, null, null, null);
        AccesoVehicularReporteProjection projection = mock(AccesoVehicularReporteProjection.class);
        List<AccesoVehicularReporteProjection> lista = List.of(projection);
        byte[] bytesMock = new byte[]{4, 5, 6};

        when(repository.findBy(any(Specification.class), any(Function.class))).thenReturn(lista);
        when(pdfStrategy.exportar(lista)).thenReturn(bytesMock);

        // Act
        byte[] result = service.exportarReporte(filtro, "pdf");

        // Assert
        assertThat(result).isEqualTo(bytesMock);
        verify(repository, times(1)).findBy(any(Specification.class), any(Function.class));
        verify(pdfStrategy, times(1)).exportar(lista);
    }

    @Test
    @DisplayName("debe_lanzar_422_si_formato_no_soportado")
    @SuppressWarnings("unchecked")
    void debe_lanzar_422_si_formato_no_soportado() {
        // Arrange
        var filtro = new ReporteAccesoFiltroRequest(null, null, null, null, null, null, null);
        AccesoVehicularReporteProjection projection = mock(AccesoVehicularReporteProjection.class);
        List<AccesoVehicularReporteProjection> lista = List.of(projection);

        when(repository.findBy(any(Specification.class), any(Function.class))).thenReturn(lista);

        // Act & Assert
        assertThatThrownBy(() -> service.exportarReporte(filtro, "invalido"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Formato de exportación no soportado: invalido");
    }
}
