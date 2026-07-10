package com.ims.tenant;

import com.ims.common.NotFoundException;

import java.util.UUID;

/**
 * Hibernate @Filter is applied to queries but NOT to primary-key loads
 * (EntityManager.find / JpaRepository.findById). This guard closes that gap:
 * call it on any tenant-owned entity fetched by id so a cross-tenant row is
 * treated as not found. SUPER_ADMIN bypasses.
 */
public final class TenantGuard {

    private TenantGuard() {
    }

    public static <T extends TenantAwareEntity> T owned(T entity) {
        if (entity == null || TenantContext.isSuperAdmin()) {
            return entity;
        }
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equals(entity.getInstituteId())) {
            throw new NotFoundException("Not found");
        }
        return entity;
    }
}
