package com.ims.tenant;

import com.ims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Base for every tenant-owned entity. Carries institute_id and defines the
 * "tenantFilter" Hibernate filter. Concrete entities must also add
 * {@code @Filter(name = TENANT_FILTER, condition = "institute_id = :tenantId")}
 * so the row-level scope is applied to their queries.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
@FilterDef(
        name = TenantAwareEntity.TENANT_FILTER,
        parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
public abstract class TenantAwareEntity extends BaseEntity {

    public static final String TENANT_FILTER = "tenantFilter";
    public static final String TENANT_CONDITION = "institute_id = :tenantId";

    @Column(name = "institute_id", nullable = false, updatable = false)
    private UUID instituteId;
}
