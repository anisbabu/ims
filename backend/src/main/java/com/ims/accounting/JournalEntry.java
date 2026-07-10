package com.ims.accounting;

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

/** A journal voucher header. Lines must balance (total debit == total credit) to post. */
@Getter
@Setter
@Entity
@Table(name = "journal_entry")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class JournalEntry extends TenantAwareEntity {

    @Column(name = "financial_year_id", nullable = false)
    private UUID financialYearId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(length = 64)
    private String reference;

    @Column(length = 255)
    private String narration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private JournalSource source = JournalSource.MANUAL;

    /** Origin document type for AUTO entries, e.g. FEE_PAYMENT. */
    @Column(name = "source_type", length = 32)
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(nullable = false)
    private boolean posted = false;
}
