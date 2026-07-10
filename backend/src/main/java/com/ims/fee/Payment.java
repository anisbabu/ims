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

/** A payment recorded against a fee. */
@Getter
@Setter
@Entity
@Table(name = "payment")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Payment extends TenantAwareEntity {

    @Column(name = "fee_id", nullable = false)
    private UUID feeId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method = PaymentMethod.CASH;

    @Column(length = 128)
    private String reference;

    @Column(name = "paid_on")
    private LocalDate paidOn;
}
