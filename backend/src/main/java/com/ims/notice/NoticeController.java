package com.ims.notice;

import com.ims.common.PageResponse;
import com.ims.notice.dto.NoticeDtos.NoticeResponse;
import com.ims.notice.dto.NoticeDtos.SaveNotice;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public NoticeResponse create(@Valid @RequestBody SaveNotice req) {
        return noticeService.create(req);
    }

    @GetMapping
    public PageResponse<NoticeResponse> list(@RequestParam(required = false) String q,
                                             Pageable pageable) {
        return noticeService.list(q, pageable);
    }

    @GetMapping("/{id}")
    public NoticeResponse get(@PathVariable UUID id) {
        return noticeService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(STAFF)
    public NoticeResponse update(@PathVariable UUID id, @Valid @RequestBody SaveNotice req) {
        return noticeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(STAFF)
    public void delete(@PathVariable UUID id) {
        noticeService.delete(id);
    }
}
