package com.ims.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Enables the Hibernate tenant filter on the active Session at the start of every
 * transactional service/repository method. Ordered to run INSIDE the transaction
 * (see {@link com.ims.config.PersistenceConfig} which pins the transaction advisor
 * to a higher precedence).
 *
 * <p>SUPER_ADMIN requests skip the filter for cross-tenant platform administration.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.ims..*Service.*(..))")
    public void enableTenantFilter() {
        if (TenantContext.isSuperAdmin()) {
            return;
        }
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter(TenantAwareEntity.TENANT_FILTER);
        if (filter == null) {
            session.enableFilter(TenantAwareEntity.TENANT_FILTER).setParameter("tenantId", tenantId);
        }
    }
}
