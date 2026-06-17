package com.estaciona.api.modules.auditoria;

import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Repositorio JPA para LogAuditoria. Soporta filtros dinámicos y proyecciones.
 */
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, UUID>, JpaSpecificationExecutor<LogAuditoria> {
}

