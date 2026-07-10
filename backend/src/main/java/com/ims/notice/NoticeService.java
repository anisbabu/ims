package com.ims.notice;

import com.ims.academic.GradeRepository;
import com.ims.academic.SectionRepository;
import com.ims.auth.Role;
import com.ims.auth.User;
import com.ims.auth.UserRepository;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.notice.dto.NoticeDtos.NoticeResponse;
import com.ims.notice.dto.NoticeDtos.SaveNotice;
import com.ims.tenant.TenantContext;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationRepository notificationRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;

    public NoticeService(NoticeRepository noticeRepository,
                         NotificationRepository notificationRepository,
                         GradeRepository gradeRepository,
                         SectionRepository sectionRepository,
                         UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.notificationRepository = notificationRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NoticeResponse create(SaveNotice req) {
        if (TenantContext.getTenantId() == null) {
            throw new BadRequestException("Notices are institute-scoped; log in as an institute user");
        }
        Notice n = new Notice();
        apply(n, req);
        n = noticeRepository.save(n);
        fanOut(n);
        return NoticeResponse.from(n);
    }

    @Transactional
    public NoticeResponse update(UUID id, SaveNotice req) {
        Notice n = noticeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Notice not found"));
        apply(n, req);
        return NoticeResponse.from(noticeRepository.save(n));
    }

    @Transactional
    public void delete(UUID id) {
        Notice n = noticeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Notice not found"));
        noticeRepository.delete(n);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse> list(String q, Pageable pageable) {
        var page = (q != null && !q.isBlank())
                ? noticeRepository.findByTitleContainingIgnoreCase(q.trim(), pageable)
                : noticeRepository.findAll(pageable);
        return PageResponse.from(page, NoticeResponse::from);
    }

    @Transactional(readOnly = true)
    public NoticeResponse get(UUID id) {
        return NoticeResponse.from(noticeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Notice not found")));
    }

    private void apply(Notice n, SaveNotice req) {
        n.setTitle(req.title());
        n.setBody(req.body());
        n.setAudience(req.audience() != null ? req.audience() : NoticeAudience.ALL);
        if (req.gradeId() != null) {
            gradeRepository.findById(req.gradeId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Grade not found"));
        }
        if (req.sectionId() != null) {
            sectionRepository.findById(req.sectionId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Section not found"));
        }
        n.setGradeId(req.gradeId());
        n.setSectionId(req.sectionId());
        n.setPublishDate(req.publishDate() != null ? req.publishDate() : LocalDate.now());
        n.setExpiresOn(req.expiresOn());
        if (req.expiresOn() != null && req.expiresOn().isBefore(n.getPublishDate())) {
            throw new BadRequestException("Expiry cannot be before publish date");
        }
        n.setPinned(Boolean.TRUE.equals(req.pinned()));
    }

    /** Creates one in-app notification per user in the notice's audience. */
    private void fanOut(Notice notice) {
        UUID instituteId = notice.getInstituteId();
        if (instituteId == null) {
            return;
        }
        List<User> recipients = switch (notice.getAudience()) {
            case ALL -> userRepository.findByInstituteId(instituteId);
            case STUDENTS -> userRepository.findByInstituteIdAndRoleIn(instituteId, List.of(Role.STUDENT));
            case GUARDIANS -> userRepository.findByInstituteIdAndRoleIn(instituteId, List.of(Role.GUARDIAN));
            case TEACHERS -> userRepository.findByInstituteIdAndRoleIn(instituteId, List.of(Role.TEACHER));
            case STAFF -> userRepository.findByInstituteIdAndRoleIn(
                    instituteId, List.of(Role.TEACHER, Role.INSTITUTE_ADMIN));
        };
        List<Notification> notifications = recipients.stream().map(u -> {
            Notification x = new Notification();
            x.setInstituteId(instituteId);
            x.setUserId(u.getId());
            x.setType(NotificationType.NOTICE);
            x.setTitle(notice.getTitle());
            x.setBody(notice.getBody());
            x.setLink("/dashboard/notices");
            return x;
        }).toList();
        notificationRepository.saveAll(notifications);
    }
}
