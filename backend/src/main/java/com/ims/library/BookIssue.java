package com.ims.library;

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

/** A book loan to a student. */
@Getter
@Setter
@Entity
@Table(name = "book_issue")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class BookIssue extends TenantAwareEntity {

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookIssueStatus status = BookIssueStatus.ISSUED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fine = BigDecimal.ZERO;
}
