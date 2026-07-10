package com.ims.exam;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.util.UUID;

/** One student's mark in one subject of one exam. */
@Getter
@Setter
@Entity
@Table(name = "mark",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "student_id", "subject_id"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Mark extends TenantAwareEntity {

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "max_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxMarks = new BigDecimal("100");

    @Column(name = "obtained_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal obtainedMarks = BigDecimal.ZERO;

    @Column(length = 255)
    private String remarks;
}
