package com.ims.notice;

import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.notice.dto.NoticeDtos.NotificationResponse;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> myList(UUID userId, boolean unreadOnly, Pageable pageable) {
        var page = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNull(userId, pageable)
                : notificationRepository.findByUserId(userId, pageable);
        return PageResponse.from(page, NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markRead(UUID id, UUID userId) {
        Notification n = notificationRepository.findById(id).map(TenantGuard::owned)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (n.getReadAt() == null) {
            n.setReadAt(Instant.now());
            n = notificationRepository.save(n);
        }
        return NotificationResponse.from(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        var unread = notificationRepository.findByUserIdAndReadAtIsNull(userId);
        Instant now = Instant.now();
        unread.forEach(n -> n.setReadAt(now));
        notificationRepository.saveAll(unread);
    }
}
