package com.ims.exam;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/** Category of exam, e.g. Midterm, Final, Class Test, Assignment. */
@Getter
@Setter
@Entity
@Table(name = "exam_type",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "name"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class ExamType extends TenantAwareEntity {

    @Column(nullable = false, length = 64)
    private String name;

    /** Optional weight (percent) toward a cumulative result. */
    @Column(name = "weight_percent")
    private Integer weightPercent;
}
