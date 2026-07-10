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

import java.time.Instant;
import java.util.UUID;

/** An in-app notification addressed to one user. read_at null = unread. */
@Getter
@Setter
@Entity
@Table(name = "notification")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class Notification extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationType type = NotificationType.GENERAL;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(length = 255)
    private String link;

    @Column(name = "read_at")
    private Instant readAt;
}
