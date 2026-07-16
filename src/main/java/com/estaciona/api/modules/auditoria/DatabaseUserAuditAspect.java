package com.estaciona.api.modules.auditoria;

import com.estaciona.api.security.CustomUserDetails;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

/**
 * Aspecto para inyectar el usuario actual de Spring Security en la sesión de PostgreSQL.
 * Esto permite que los triggers de base de datos registren correctamente quién ejecutó el cambio.
 */
@Aspect
@Component
public class DatabaseUserAuditAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.estaciona.api.modules..*Service.*(..))")
    public void setSessionUser() {
        System.out.println("[AUDIT ASPECT] Intercepted service method call");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                System.out.println("[AUDIT ASPECT] Authenticated user principal: " + authentication.getPrincipal().getClass().getName());
                if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                    UUID userId = userDetails.obtenerUsuarioId();
                    System.out.println("[AUDIT ASPECT] Setting PostgreSQL session user ID: " + userId);
                    Object result = entityManager.createNativeQuery("SELECT set_config('app.current_user_id', :userId, true)")
                            .setParameter("userId", userId.toString())
                            .getSingleResult();
                    System.out.println("[AUDIT ASPECT] PostgreSQL set_config returned: " + result);
                } else {
                    System.out.println("[AUDIT ASPECT] Principal is not CustomUserDetails");
                }
            } else {
                System.out.println("[AUDIT ASPECT] No authenticated user found in SecurityContext");
            }
        } catch (Exception e) {
            System.err.println("[AUDIT ASPECT] Error setting session user ID: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
