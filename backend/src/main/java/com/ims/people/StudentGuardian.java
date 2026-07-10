package com.ims.people;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.UUID;

/** Links a student to a guardian with a relationship type. */
@Getter
@Setter
@Entity
@Table(name = "student_guardian",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "guardian_id"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class StudentGuardian extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Relation relation = Relation.GUARDIAN;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;
}
