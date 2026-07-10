package com.ims.exam;

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

/** An exam event tied to an academic year and (optionally) a grade. */
@Getter
@Setter
@Entity
@Table(name = "exam")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Exam extends TenantAwareEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "exam_type_id", nullable = false)
    private UUID examTypeId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExamStatus status = ExamStatus.SCHEDULED;
}
