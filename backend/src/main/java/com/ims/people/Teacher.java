package com.ims.people;

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

@Getter
@Setter
@Entity
@Table(name = "teacher")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Teacher extends TenantAwareEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Designation designation = Designation.SUBJECT;

    private String phone;
    private String email;
    private String address;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PersonStatus status = PersonStatus.ACTIVE;
}
