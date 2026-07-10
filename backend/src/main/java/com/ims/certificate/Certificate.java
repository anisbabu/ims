package com.ims.certificate;

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

import java.time.LocalDate;
import java.util.UUID;

/** A document issued to a student (transfer/character/completion/marksheet, etc.). */
@Getter
@Setter
@Entity
@Table(name = "certificate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "serial_no"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Certificate extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CertificateType type = CertificateType.OTHER;

    @Column(name = "serial_no", length = 64)
    private String serialNo;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(columnDefinition = "text")
    private String content;
}
