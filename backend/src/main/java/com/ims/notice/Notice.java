package com.ims.notice;

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
import java.util.UUID;

/** A notice-board announcement, optionally targeted at a grade/section or audience group. */
@Getter
@Setter
@Entity
@Table(name = "notice")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Notice extends TenantAwareEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NoticeAudience audience = NoticeAudience.ALL;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Column(nullable = false)
    private boolean pinned = false;
}
