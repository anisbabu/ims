package com.ims.people;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Entity
@Table(name = "guardian")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Guardian extends TenantAwareEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;
    private String email;
    private String occupation;
    private String address;
}
