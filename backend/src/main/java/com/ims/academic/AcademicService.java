package com.ims.academic;

import com.ims.academic.dto.AcademicDtos.CreateGrade;
import com.ims.academic.dto.AcademicDtos.CreateSection;
import com.ims.academic.dto.AcademicDtos.CreateYear;
import com.ims.academic.dto.AcademicDtos.GradeResponse;
import com.ims.academic.dto.AcademicDtos.SectionResponse;
import com.ims.academic.dto.AcademicDtos.UpdateGrade;
import com.ims.academic.dto.AcademicDtos.UpdateSection;
import com.ims.academic.dto.AcademicDtos.UpdateYear;
import com.ims.academic.dto.AcademicDtos.YearResponse;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AcademicService {

    private final AcademicYearRepository yearRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;

    public AcademicService(AcademicYearRepository yearRepository,
                           GradeRepository gradeRepository,
                           SectionRepository sectionRepository) {
        this.yearRepository = yearRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
    }

    // ---- Academic Year ----

    @Transactional
    public YearResponse createYear(CreateYear req) {
        AcademicYear y = new AcademicYear();
        y.setName(req.name());
        y.setStartDate(req.startDate());
        y.setEndDate(req.endDate());
        y.setCurrent(req.current());
        if (req.current()) {
            clearCurrentYears();
        }
        return YearResponse.from(yearRepository.save(y));
    }

    @Transactional(readOnly = true)
    public List<YearResponse> listYears() {
        return yearRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicYear::getName).reversed())
                .map(YearResponse::from).toList();
    }

    @Transactional
    public YearResponse updateYear(UUID id, UpdateYear req) {
        AcademicYear y = yearRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Academic year not found"));
        if (req.name() != null) y.setName(req.name());
        if (req.startDate() != null) y.setStartDate(req.startDate());
        if (req.endDate() != null) y.setEndDate(req.endDate());
        if (req.current() != null) {
            if (req.current()) clearCurrentYears();
            y.setCurrent(req.current());
        }
        return YearResponse.from(y);
    }

    private void clearCurrentYears() {
        yearRepository.findByCurrentTrue().forEach(existing -> existing.setCurrent(false));
    }

    // ---- Grade ----

    @Transactional
    public GradeResponse createGrade(CreateGrade req) {
        Grade g = new Grade();
        g.setName(req.name());
        g.setOrderNo(req.orderNo());
        return GradeResponse.from(gradeRepository.save(g));
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> listGrades() {
        return gradeRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Grade::getOrderNo).thenComparing(Grade::getName))
                .map(GradeResponse::from).toList();
    }

    @Transactional
    public GradeResponse updateGrade(UUID id, UpdateGrade req) {
        Grade g = gradeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Grade not found"));
        if (req.name() != null) g.setName(req.name());
        if (req.orderNo() != null) g.setOrderNo(req.orderNo());
        return GradeResponse.from(g);
    }

    // ---- Section ----

    @Transactional
    public SectionResponse createSection(CreateSection req) {
        gradeRepository.findById(req.gradeId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Grade not found: " + req.gradeId()));
        Section s = new Section();
        s.setGradeId(req.gradeId());
        s.setName(req.name());
        s.setClassTeacherId(req.classTeacherId());
        s.setCapacity(req.capacity());
        return SectionResponse.from(sectionRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> listSections(UUID gradeId) {
        List<Section> sections = (gradeId != null)
                ? sectionRepository.findByGradeId(gradeId)
                : sectionRepository.findAll();
        return sections.stream().map(SectionResponse::from).toList();
    }

    @Transactional
    public SectionResponse updateSection(UUID id, UpdateSection req) {
        Section s = sectionRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Section not found"));
        if (req.name() != null) s.setName(req.name());
        if (req.classTeacherId() != null) s.setClassTeacherId(req.classTeacherId());
        if (req.capacity() != null) s.setCapacity(req.capacity());
        return SectionResponse.from(s);
    }
}
