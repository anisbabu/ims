package com.ims.staff;

import com.ims.people.Gender;
import com.ims.people.PersonStatus;
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

/** Non-teaching staff member (accountant, clerk, driver, security, etc.). */
@Getter
@Setter
@Entity
@Table(name = "employee")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Employee extends TenantAwareEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Free-text role, e.g. Accountant, Clerk, Driver, Security. */
    @Column(length = 64)
    private String designation;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender;

    private String phone;
    private String email;
    private String address;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PersonStatus status = PersonStatus.ACTIVE;
}
