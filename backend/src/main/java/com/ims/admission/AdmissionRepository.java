package com.ims.admission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdmissionRepository extends JpaRepository<Admission, UUID> {
    Page<Admission> findByStatus(AdmissionStatus status, Pageable pageable);
    List<Admission> findByStudentId(UUID studentId);
    List<Admission> findByAcademicYearIdAndGradeIdAndStatus(UUID academicYearId, UUID gradeId, AdmissionStatus status);
}
