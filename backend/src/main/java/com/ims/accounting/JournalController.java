package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.CreateJournal;
import com.ims.accounting.dto.AccountingDtos.JournalResponse;
import com.ims.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounting/journals")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalResponse create(@Valid @RequestBody CreateJournal req) {
        return journalService.create(req);
    }

    @GetMapping
    public PageResponse<JournalResponse> list(@RequestParam(required = false) UUID financialYearId,
                                              Pageable pageable) {
        return journalService.list(financialYearId, pageable);
    }

    @GetMapping("/{id}")
    public JournalResponse get(@PathVariable UUID id) {
        return journalService.get(id);
    }

    @PatchMapping("/{id}/post")
    public JournalResponse post(@PathVariable UUID id) {
        return journalService.post(id);
    }
}
