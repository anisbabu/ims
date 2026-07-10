package com.ims.people;

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

@Getter
@Setter
@Entity
@Table(name = "student",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "reg_no"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Student extends TenantAwareEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "reg_no", length = 64)
    private String regNo;

    @Column(name = "roll_no", length = 64)
    private String rollNo;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender;

    @Column(name = "photo_url")
    private String photoUrl;

    private String phone;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PersonStatus status = PersonStatus.ACTIVE;
}
