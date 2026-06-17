package com.estaciona.api.modules.auditoria;

import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResponse;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResumenProjection;
import com.estaciona.api.modules.auditoria.dto.AuditoriaFiltroRequest;
import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaEventoFactory auditoriaEventoFactory;

    @InjectMocks
    private AuditoriaServiceImpl service;

    private Usuario usuario;
    private AuditoriaEventoRequest request;
    private LogAuditoria logBuilt;
    private final UUID usuarioId = UUID.randomUUID();
    private final UUID logId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombreCompleto("Admin Test");

        request = new AuditoriaEventoRequest(
                "vehiculos", "reg123", "INSERT", null, "{\"color\":\"rojo\"}");

        logBuilt = LogAuditoria.builder()
                .id(logId)
                .usuario(usuario)
                .tablaAfectada("vehiculos")
                .registroId("reg123")
                .accion("INSERT")
                .valoresAnteriores(null)
                .valoresNuevos("{\"color\":\"rojo\"}")
                .fecha(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("debe_registrar_evento_manual_correctamente")
    void debe_registrar_evento_manual_correctamente() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(auditoriaEventoFactory.crear(request, usuario)).thenReturn(logBuilt);
        when(logAuditoriaRepository.save(logBuilt)).thenReturn(logBuilt);

        // Act
        AuditoriaEventoResponse response = service.registrarEvento(request, usuarioId);

        // Assert
        assertThat(response.id()).isEqualTo(logId);
        assertThat(response.usuario()).isEqualTo("Admin Test");
        assertThat(response.tablaAfectada()).isEqualTo("vehiculos");
        verify(logAuditoriaRepository, times(1)).save(logBuilt);
    }

    @Test
    @DisplayName("debe_lanzar_404_si_usuario_no_existe_en_registro")
    void debe_lanzar_404_si_usuario_no_existe_en_registro() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.registrarEvento(request, usuarioId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con id: " + usuarioId);

        verify(logAuditoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_listar_eventos_paginados")
    void debe_listar_eventos_paginados() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        AuditoriaEventoResumenProjection projection = mock(AuditoriaEventoResumenProjection.class);
        Page<AuditoriaEventoResumenProjection> page = new PageImpl<>(List.of(projection), pageable, 1);

        when(logAuditoriaRepository.findBy(any(Specification.class), any())).thenReturn(page);

        // Act
        Page<AuditoriaEventoResumenProjection> result = service.listarEventos(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(logAuditoriaRepository, times(1)).findBy(any(Specification.class), any());
    }

    @Test
    @DisplayName("debe_filtrar_eventos_dinamicamente")
    void debe_filtrar_eventos_dinamicamente() {
        // Arrange
        var filtro = new AuditoriaFiltroRequest("vehiculos", "INSERT", usuarioId, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        AuditoriaEventoResumenProjection projection = mock(AuditoriaEventoResumenProjection.class);
        Page<AuditoriaEventoResumenProjection> page = new PageImpl<>(List.of(projection), pageable, 1);

        when(logAuditoriaRepository.findBy(any(Specification.class), any())).thenReturn(page);

        // Act
        Page<AuditoriaEventoResumenProjection> result = service.filtrarEventos(filtro, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(logAuditoriaRepository, times(1)).findBy(any(Specification.class), any());
    }

    @Test
    @DisplayName("debe_obtener_detalle_de_evento_existente")
    void debe_obtener_detalle_de_evento_existente() {
        // Arrange
        when(logAuditoriaRepository.findById(logId)).thenReturn(Optional.of(logBuilt));

        // Act
        AuditoriaEventoResponse response = service.obtenerDetalle(logId);

        // Assert
        assertThat(response.id()).isEqualTo(logId);
        assertThat(response.registroId()).isEqualTo("reg123");
        assertThat(response.valoresNuevos()).isEqualTo("{\"color\":\"rojo\"}");
    }

    @Test
    @DisplayName("debe_lanzar_404_si_detalle_de_evento_no_existe")
    void debe_lanzar_404_si_detalle_de_evento_no_existe() {
        // Arrange
        when(logAuditoriaRepository.findById(logId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.obtenerDetalle(logId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Log de auditoría no encontrado con id: " + logId);
    }
}
