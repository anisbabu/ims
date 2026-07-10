package com.ims.notice.dto;

import com.ims.notice.Notice;
import com.ims.notice.NoticeAudience;
import com.ims.notice.Notification;
import com.ims.notice.NotificationType;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class NoticeDtos {

    private NoticeDtos() {
    }

    public record SaveNotice(
            @NotBlank String title,
            String body,
            NoticeAudience audience,
            UUID gradeId,
            UUID sectionId,
            LocalDate publishDate,
            LocalDate expiresOn,
            Boolean pinned) {
    }

    public record NoticeResponse(
            UUID id, String title, String body, NoticeAudience audience,
            UUID gradeId, UUID sectionId, LocalDate publishDate, LocalDate expiresOn,
            boolean pinned, Instant createdAt) {
        public static NoticeResponse from(Notice n) {
            return new NoticeResponse(n.getId(), n.getTitle(), n.getBody(), n.getAudience(),
                    n.getGradeId(), n.getSectionId(), n.getPublishDate(), n.getExpiresOn(),
                    n.isPinned(), n.getCreatedAt());
        }
    }

    public record NotificationResponse(
            UUID id, NotificationType type, String title, String body,
            String link, Instant readAt, Instant createdAt) {
        public static NotificationResponse from(Notification n) {
            return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(),
                    n.getLink(), n.getReadAt(), n.getCreatedAt());
        }
    }

    public record UnreadCount(long count) {
    }
}
