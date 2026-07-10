package com.ims.fee;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A fee charge/invoice raised against a student. paidAmount is the running total of payments. */
@Getter
@Setter
@Entity
@Table(name = "fee")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Fee extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "academic_year_id")
    private UUID academicYearId;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FeeStatus status = FeeStatus.PENDING;
}
