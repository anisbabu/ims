package com.ims.fee;

import com.ims.academic.AcademicYearRepository;
import com.ims.academic.GradeRepository;
import com.ims.admission.Admission;
import com.ims.admission.AdmissionRepository;
import com.ims.admission.AdmissionStatus;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.fee.dto.FeeDtos.CreateFee;
import com.ims.fee.dto.FeeStructureDtos.FeeStructureResponse;
import com.ims.fee.dto.FeeStructureDtos.GenerateFees;
import com.ims.fee.dto.FeeStructureDtos.GenerateResult;
import com.ims.fee.dto.FeeStructureDtos.SaveFeeStructure;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeRepository feeRepository;
    private final FeeService feeService;
    private final AdmissionRepository admissionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;

    public FeeStructureService(FeeStructureRepository feeStructureRepository,
                               FeeRepository feeRepository,
                               FeeService feeService,
                               AdmissionRepository admissionRepository,
                               AcademicYearRepository academicYearRepository,
                               GradeRepository gradeRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.feeRepository = feeRepository;
        this.feeService = feeService;
        this.admissionRepository = admissionRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional
    public FeeStructureResponse create(SaveFeeStructure req) {
        validateRefs(req);
        if (feeStructureRepository.existsByAcademicYearIdAndGradeIdAndTitle(
                req.academicYearId(), req.gradeId(), req.title())) {
            throw new BadRequestException("A fee head with this title already exists for that year and grade");
        }
        FeeStructure s = new FeeStructure();
        apply(s, req);
        return FeeStructureResponse.from(feeStructureRepository.save(s));
    }

    @Transactional
    public FeeStructureResponse update(UUID id, SaveFeeStructure req) {
        FeeStructure s = require(id);
        validateRefs(req);
        apply(s, req);
        return FeeStructureResponse.from(feeStructureRepository.save(s));
    }

    @Transactional
    public void delete(UUID id) {
        feeStructureRepository.delete(require(id));
    }

    @Transactional(readOnly = true)
    public List<FeeStructureResponse> list(UUID academicYearId, UUID gradeId) {
        List<FeeStructure> rows;
        if (academicYearId != null && gradeId != null) {
            rows = feeStructureRepository.findByAcademicYearIdAndGradeId(academicYearId, gradeId);
        } else if (academicYearId != null) {
            rows = feeStructureRepository.findByAcademicYearId(academicYearId);
        } else {
            rows = feeStructureRepository.findAll();
        }
        return rows.stream().map(FeeStructureResponse::from).toList();
    }

    /**
     * Creates one Fee per enrolled student per fee head of the grade/year.
     * Idempotent: a student already billed for a head (same year + title) is skipped,
     * so re-running after adding late admissions only bills the newcomers.
     */
    @Transactional
    public GenerateResult generate(GenerateFees req) {
        List<FeeStructure> heads = feeStructureRepository
                .findByAcademicYearIdAndGradeId(req.academicYearId(), req.gradeId());
        if (heads.isEmpty()) {
            throw new BadRequestException("No fee structure defined for that year and grade");
        }
        List<Admission> enrolled = admissionRepository.findByAcademicYearIdAndGradeIdAndStatus(
                req.academicYearId(), req.gradeId(), AdmissionStatus.ENROLLED);
        int created = 0, skipped = 0;
        for (Admission adm : enrolled) {
            for (FeeStructure head : heads) {
                if (feeRepository.existsByStudentIdAndAcademicYearIdAndTitle(
                        adm.getStudentId(), req.academicYearId(), head.getTitle())) {
                    skipped++;
                    continue;
                }
                feeService.createFee(new CreateFee(adm.getStudentId(), req.academicYearId(),
                        head.getTitle(), head.getAmount(), head.getDueDate()));
                created++;
            }
        }
        return new GenerateResult(enrolled.size(), created, skipped);
    }

    private void apply(FeeStructure s, SaveFeeStructure req) {
        s.setAcademicYearId(req.academicYearId());
        s.setGradeId(req.gradeId());
        s.setTitle(req.title());
        s.setAmount(req.amount());
        s.setDueDate(req.dueDate());
    }

    private void validateRefs(SaveFeeStructure req) {
        academicYearRepository.findById(req.academicYearId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Academic year not found"));
        gradeRepository.findById(req.gradeId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Grade not found"));
    }

    private FeeStructure require(UUID id) {
        return feeStructureRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Fee structure not found"));
    }
}
