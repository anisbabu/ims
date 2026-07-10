package com.ims.admission;

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

@Getter
@Setter
@Entity
@Table(name = "admission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "admission_no"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Admission extends TenantAwareEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "admission_no", length = 64)
    private String admissionNo;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AdmissionStatus status = AdmissionStatus.APPLIED;
}
