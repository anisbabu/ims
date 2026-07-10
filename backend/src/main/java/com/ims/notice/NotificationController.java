package com.ims.notice;

import com.ims.auth.SecurityUser;
import com.ims.common.PageResponse;
import com.ims.notice.dto.NoticeDtos.NotificationResponse;
import com.ims.notice.dto.NoticeDtos.UnreadCount;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** The current user's in-app notifications. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(@AuthenticationPrincipal SecurityUser me,
                                                   @RequestParam(defaultValue = "false") boolean unread,
                                                   Pageable pageable) {
        return notificationService.myList(me.getId(), unread, pageable);
    }

    @GetMapping("/unread-count")
    public UnreadCount unreadCount(@AuthenticationPrincipal SecurityUser me) {
        return new UnreadCount(notificationService.unreadCount(me.getId()));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id,
                                         @AuthenticationPrincipal SecurityUser me) {
        return notificationService.markRead(id, me.getId());
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal SecurityUser me) {
        notificationService.markAllRead(me.getId());
    }
}
