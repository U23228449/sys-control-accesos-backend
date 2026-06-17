package com.estaciona.api.modules.auditoria;

import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResponse;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResumenProjection;
import com.estaciona.api.modules.auditoria.dto.AuditoriaFiltroRequest;
import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import com.estaciona.api.modules.auditoria.spec.AuditoriaEventoSpecifications;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del servicio de auditoría.
 */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaEventoFactory auditoriaEventoFactory;

    public AuditoriaServiceImpl(LogAuditoriaRepository logAuditoriaRepository,
                                UsuarioRepository usuarioRepository,
                                AuditoriaEventoFactory auditoriaEventoFactory) {
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaEventoFactory = auditoriaEventoFactory;
    }

    @Override
    @Transactional
    public AuditoriaEventoResponse registrarEvento(AuditoriaEventoRequest request, UUID usuarioId) {
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));
        }

        LogAuditoria log = auditoriaEventoFactory.crear(request, usuario);
        LogAuditoria guardado = logAuditoriaRepository.save(log);

        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaEventoResumenProjection> listarEventos(Pageable pageable) {
        return logAuditoriaRepository.findBy(Specification.where(null), q -> q.as(AuditoriaEventoResumenProjection.class).page(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaEventoResumenProjection> filtrarEventos(AuditoriaFiltroRequest filtro, Pageable pageable) {
        Specification<LogAuditoria> spec = AuditoriaEventoSpecifications.construir(filtro);
        return logAuditoriaRepository.findBy(spec, q -> q.as(AuditoriaEventoResumenProjection.class).page(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditoriaEventoResponse obtenerDetalle(UUID id) {
        LogAuditoria log = logAuditoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log de auditoría no encontrado con id: " + id));
        return toResponse(log);
    }

    /** Mapeo manual de LogAuditoria a AuditoriaEventoResponse. */
    private AuditoriaEventoResponse toResponse(LogAuditoria log) {
        return new AuditoriaEventoResponse(
                log.getId(),
                log.getUsuario() != null ? log.getUsuario().getNombreCompleto() : null,
                log.getTablaAfectada(),
                log.getRegistroId(),
                log.getAccion(),
                log.getValoresAnteriores(),
                log.getValoresNuevos(),
                log.getFecha()
        );
    }
}
