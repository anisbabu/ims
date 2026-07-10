package com.ims.admission;

import com.ims.admission.dto.AdmissionDtos.AdmissionResponse;
import com.ims.admission.dto.AdmissionDtos.CreateAdmission;
import com.ims.admission.dto.AdmissionDtos.UpdateStatus;
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
@RequestMapping("/api/admissions")
public class AdmissionController {

    private static final String ADMIN = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')";

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public AdmissionResponse create(@Valid @RequestBody CreateAdmission req) {
        return admissionService.create(req);
    }

    @GetMapping
    public PageResponse<AdmissionResponse> list(@RequestParam(required = false) AdmissionStatus status,
                                                Pageable pageable) {
        return admissionService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public AdmissionResponse get(@PathVariable UUID id) {
        return admissionService.get(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(ADMIN)
    public AdmissionResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatus req) {
        return admissionService.updateStatus(id, req.status());
    }
}
