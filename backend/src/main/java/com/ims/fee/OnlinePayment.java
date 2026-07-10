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
import java.time.Instant;
import java.util.UUID;

/**
 * A gateway checkout attempt for a fee. Created PENDING; the provider callback
 * (mock: the payer confirming the hosted checkout page) moves it to SUCCESS and
 * records the ledger Payment.
 */
@Getter
@Setter
@Entity
@Table(name = "online_payment")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class OnlinePayment extends TenantAwareEntity {

    @Column(name = "fee_id", nullable = false)
    private UUID feeId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /** The user who initiated checkout; only they may confirm/cancel it. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 32)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OnlinePaymentStatus status = OnlinePaymentStatus.PENDING;

    @Column(length = 128)
    private String reference;

    @Column(name = "paid_at")
    private Instant paidAt;
}
