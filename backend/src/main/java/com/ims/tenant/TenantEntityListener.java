package com.ims.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * On insert, stamps institute_id from the current {@link TenantContext} when not already set.
 * Never trusts a client-supplied tenant id for tenant-scoped writes.
 */
public class TenantEntityListener {

    @PrePersist
    public void prePersist(TenantAwareEntity entity) {
        if (entity.getInstituteId() == null) {
            UUID tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                entity.setInstituteId(tenantId);
            }
        }
    }
}
