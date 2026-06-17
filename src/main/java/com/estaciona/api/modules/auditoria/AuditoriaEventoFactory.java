package com.estaciona.api.modules.auditoria;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Factoría para construir entidades de LogAuditoria aplicando validaciones de negocio.
 */
@Component
public class AuditoriaEventoFactory {

    /**
     * Crea y valida un objeto LogAuditoria a partir de la petición manual y el usuario.
     *
     * @param request Datos de auditoría.
     * @param usuario Usuario de sesión (puede ser null).
     * @return LogAuditoria listo para persistir.
     */
    public LogAuditoria crear(AuditoriaEventoRequest request, Usuario usuario) {
        String accion = request.accion() != null ? request.accion().toUpperCase().trim() : null;

        // Validar acción
        if (accion == null || (!"INSERT".equals(accion) && !"UPDATE".equals(accion) && !"DELETE".equals(accion))) {
            throw new BusinessRuleException("La acción de auditoría debe ser INSERT, UPDATE o DELETE.");
        }

        // Reglas cruzadas
        if ("INSERT".equals(accion)) {
            if (request.valoresAnteriores() != null && !request.valoresAnteriores().trim().isEmpty()) {
                throw new BusinessRuleException("Un evento INSERT no puede contener valores anteriores.");
            }
        }

        if ("DELETE".equals(accion)) {
            if (request.valoresNuevos() != null && !request.valoresNuevos().trim().isEmpty()) {
                throw new BusinessRuleException("Un evento DELETE no puede contener valores nuevos.");
            }
        }

        return LogAuditoria.builder()
                .usuario(usuario)
                .tablaAfectada(request.tablaAfectada().trim())
                .registroId(request.registroId().trim())
                .accion(accion)
                .valoresAnteriores(request.valoresAnteriores() != null ? request.valoresAnteriores().trim() : null)
                .valoresNuevos(request.valoresNuevos() != null ? request.valoresNuevos().trim() : null)
                .fecha(OffsetDateTime.now())
                .build();
    }
}
