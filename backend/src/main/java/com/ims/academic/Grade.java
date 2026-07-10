package com.ims.academic;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/** A class/grade level, e.g. "Class 5". */
@Getter
@Setter
@Entity
@Table(name = "grade",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Grade extends TenantAwareEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "order_no", nullable = false)
    private int orderNo = 0;
}
