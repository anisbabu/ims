package com.ims.exam;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/** A teachable subject, e.g. "Mathematics". */
@Getter
@Setter
@Entity
@Table(name = "subject",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Subject extends TenantAwareEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 32)
    private String code;
}
