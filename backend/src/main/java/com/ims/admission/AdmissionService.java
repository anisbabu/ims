package com.ims.admission;

import com.ims.academic.AcademicYearRepository;
import com.ims.academic.GradeRepository;
import com.ims.academic.SectionRepository;
import com.ims.admission.dto.AdmissionDtos.AdmissionResponse;
import com.ims.admission.dto.AdmissionDtos.CreateAdmission;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;

    public AdmissionService(AdmissionRepository admissionRepository,
                            StudentRepository studentRepository,
                            AcademicYearRepository academicYearRepository,
                            GradeRepository gradeRepository,
                            SectionRepository sectionRepository) {
        this.admissionRepository = admissionRepository;
        this.studentRepository = studentRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public AdmissionResponse create(CreateAdmission req) {
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found: " + req.studentId()));
        academicYearRepository.findById(req.academicYearId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Academic year not found: " + req.academicYearId()));
        gradeRepository.findById(req.gradeId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Grade not found: " + req.gradeId()));
        if (req.sectionId() != null) {
            sectionRepository.findById(req.sectionId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Section not found: " + req.sectionId()));
        }

        Admission a = new Admission();
        a.setStudentId(req.studentId());
        a.setAcademicYearId(req.academicYearId());
        a.setGradeId(req.gradeId());
        a.setSectionId(req.sectionId());
        a.setAdmissionNo(req.admissionNo());
        a.setAdmissionDate(req.admissionDate());
        a.setStatus(AdmissionStatus.APPLIED);
        return AdmissionResponse.from(admissionRepository.save(a));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdmissionResponse> list(AdmissionStatus status, Pageable pageable) {
        Page<Admission> page = (status != null)
                ? admissionRepository.findByStatus(status, pageable)
                : admissionRepository.findAll(pageable);
        return PageResponse.from(page, AdmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public AdmissionResponse get(UUID id) {
        return AdmissionResponse.from(require(id));
    }

    @Transactional
    public AdmissionResponse updateStatus(UUID id, AdmissionStatus newStatus) {
        Admission a = require(id);
        if (a.getStatus() == AdmissionStatus.WITHDRAWN || a.getStatus() == AdmissionStatus.REJECTED) {
            throw new BadRequestException("Admission is in a terminal state: " + a.getStatus());
        }
        a.setStatus(newStatus);
        return AdmissionResponse.from(a);
    }

    private Admission require(UUID id) {
        return admissionRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Admission not found"));
    }
}
