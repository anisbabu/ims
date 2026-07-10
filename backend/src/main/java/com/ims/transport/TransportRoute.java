package com.ims.transport;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transport_route",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class TransportRoute extends TenantAwareEntity {

    @Column(nullable = false, length = 128)
    private String name;

    /** Free-text list of stops. */
    @Column(columnDefinition = "text")
    private String stops;

    @Column(precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "vehicle_id")
    private UUID vehicleId;
}
