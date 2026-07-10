package com.ims.library;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Entity
@Table(name = "book",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institute_id", "isbn"}))
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Book extends TenantAwareEntity {

    @Column(nullable = false, length = 255)
    private String title;

    private String author;

    @Column(length = 32)
    private String isbn;

    @Column(length = 64)
    private String category;

    @Column(length = 32)
    private String shelf;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies = 1;
}
