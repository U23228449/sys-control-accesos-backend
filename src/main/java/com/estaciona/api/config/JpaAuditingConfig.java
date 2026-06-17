package com.estaciona.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita la auditoría JPA para que @CreatedDate funcione en AuditableEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
