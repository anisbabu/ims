package com.ims.exam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    Page<Exam> findByAcademicYearId(UUID academicYearId, Pageable pageable);
}
