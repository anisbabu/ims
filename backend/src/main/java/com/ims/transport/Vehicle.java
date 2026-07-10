package com.ims.transport;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Entity
@Table(name = "vehicle",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "reg_no"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Vehicle extends TenantAwareEntity {

    @Column(name = "reg_no", nullable = false, length = 32)
    private String regNo;

    private String model;

    @Column(nullable = false)
    private int capacity = 0;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone", length = 32)
    private String driverPhone;
}
