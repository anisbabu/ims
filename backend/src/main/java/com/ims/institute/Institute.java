package com.ims.institute;

import com.ims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Tenant root. Its own id is the tenant key referenced by institute_id on all
 * tenant-owned entities. Not itself tenant-filtered.
 */
@Getter
@Setter
@Entity
@Table(name = "institute")
public class Institute extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    private String address;
    private String phone;
    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(columnDefinition = "text")
    private String settings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InstituteStatus status = InstituteStatus.ACTIVE;
}
