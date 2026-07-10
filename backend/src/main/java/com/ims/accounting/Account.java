package com.ims.accounting;

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

/**
 * A chart-of-accounts entry. systemKey marks well-known accounts the app posts to
 * automatically (e.g. CASH, BANK, FEE_INCOME).
 */
@Getter
@Setter
@Entity
@Table(name = "account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "code"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Account extends TenantAwareEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountType type;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "system_key", length = 32)
    private String systemKey;

    @Column(nullable = false)
    private boolean active = true;
}
