package com.ims.transport;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.util.UUID;

/** Assigns a student to a transport route + pickup stop. */
@Getter
@Setter
@Entity
@Table(name = "transport_assignment")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class TransportAssignment extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "stop_name")
    private String stopName;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
}
