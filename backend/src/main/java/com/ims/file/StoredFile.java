package com.ims.file;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A binary file (e.g. a profile photo) stored in the database. */
@Getter
@Setter
@Entity
@Table(name = "stored_file")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class StoredFile extends TenantAwareEntity {

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "data", nullable = false)
    private byte[] data;
}
