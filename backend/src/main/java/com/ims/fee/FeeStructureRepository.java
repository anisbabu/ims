package com.ims.fee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {
    List<FeeStructure> findByAcademicYearIdAndGradeId(UUID academicYearId, UUID gradeId);
    List<FeeStructure> findByAcademicYearId(UUID academicYearId);
    boolean existsByAcademicYearIdAndGradeIdAndTitle(UUID academicYearId, UUID gradeId, String title);
}
