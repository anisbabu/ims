package com.ims.hostel;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hostel_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hostel_id", "room_no"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Room extends TenantAwareEntity {

    @Column(name = "hostel_id", nullable = false)
    private UUID hostelId;

    @Column(name = "room_no", nullable = false, length = 32)
    private String roomNo;

    @Column(nullable = false)
    private int capacity = 1;

    @Column(nullable = false)
    private int occupied = 0;
}
