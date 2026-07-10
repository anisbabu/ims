package com.ims.fee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeRepository extends JpaRepository<Fee, UUID> {
    Page<Fee> findByStudentId(UUID studentId, Pageable pageable);
    Page<Fee> findByStatus(FeeStatus status, Pageable pageable);
    List<Fee> findAllByStudentId(UUID studentId);
    boolean existsByStudentIdAndAcademicYearIdAndTitle(UUID studentId, UUID academicYearId, String title);
}
