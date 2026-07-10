package com.ims.accounting;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

/** An accounting period. Journals are booked within a financial year. */
@Getter
@Setter
@Entity
@Table(name = "financial_year",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class FinancialYear extends TenantAwareEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private boolean current = false;

    @Column(nullable = false)
    private boolean closed = false;
}
