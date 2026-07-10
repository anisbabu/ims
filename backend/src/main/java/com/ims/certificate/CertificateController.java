package com.ims.certificate;

import com.ims.certificate.dto.CertificateDtos.CertificateResponse;
import com.ims.certificate.dto.CertificateDtos.IssueCertificate;
import com.ims.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final CertificateService certificateService;
    private final CertificatePdfService certificatePdfService;

    public CertificateController(CertificateService certificateService,
                                 CertificatePdfService certificatePdfService) {
        this.certificateService = certificateService;
        this.certificatePdfService = certificatePdfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public CertificateResponse issue(@Valid @RequestBody IssueCertificate req) {
        return certificateService.issue(req);
    }

    @GetMapping
    public PageResponse<CertificateResponse> list(@RequestParam(required = false) UUID studentId,
                                                  Pageable pageable) {
        return certificateService.list(studentId, pageable);
    }

    @GetMapping("/{id}")
    public CertificateResponse get(@PathVariable UUID id) {
        return certificateService.get(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        CertificatePdfService.Rendered r = certificatePdfService.render(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline().filename(r.fileName()).build());
        return new ResponseEntity<>(r.bytes(), headers, HttpStatus.OK);
    }
}
