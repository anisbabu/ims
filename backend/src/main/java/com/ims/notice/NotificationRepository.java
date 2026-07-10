package com.ims.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    Page<Notification> findByUserIdAndReadAtIsNull(UUID userId, Pageable pageable);
    List<Notification> findByUserIdAndReadAtIsNull(UUID userId);
    long countByUserIdAndReadAtIsNull(UUID userId);
}
