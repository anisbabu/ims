package com.ims.certificate;

import com.ims.certificate.dto.CertificateDtos.CertificateResponse;
import com.ims.certificate.dto.CertificateDtos.IssueCertificate;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;

    public CertificateService(CertificateRepository certificateRepository,
                              StudentRepository studentRepository) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public CertificateResponse issue(IssueCertificate req) {
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        Certificate c = new Certificate();
        c.setStudentId(req.studentId());
        c.setType(req.type());
        c.setTitle(req.title());
        c.setSerialNo(req.serialNo());
        c.setIssueDate(req.issueDate() != null ? req.issueDate() : LocalDate.now());
        c.setContent(req.content());
        return CertificateResponse.from(certificateRepository.save(c));
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> list(UUID studentId, Pageable pageable) {
        var page = (studentId != null)
                ? certificateRepository.findByStudentId(studentId, pageable)
                : certificateRepository.findAll(pageable);
        return PageResponse.from(page, CertificateResponse::from);
    }

    @Transactional(readOnly = true)
    public CertificateResponse get(UUID id) {
        return CertificateResponse.from(certificateRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Certificate not found")));
    }
}
