package com.ims.academic;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.UUID;

/** A section within a grade, e.g. "A". class_teacher_id references a Teacher. */
@Getter
@Setter
@Entity
@Table(name = "section",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "grade_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Section extends TenantAwareEntity {

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "class_teacher_id")
    private UUID classTeacherId;

    @Column(nullable = false)
    private int capacity = 0;
}
