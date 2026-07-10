package com.ims.hostel;

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

/** A student's allocation to a hostel room. */
@Getter
@Setter
@Entity
@Table(name = "hostel_allocation")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class HostelAllocation extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "hostel_id", nullable = false)
    private UUID hostelId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "allocated_date", nullable = false)
    private LocalDate allocatedDate;

    @Column(name = "vacated_date")
    private LocalDate vacatedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AllocationStatus status = AllocationStatus.ALLOCATED;
}
