package com.ims.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarkRepository extends JpaRepository<Mark, UUID> {
    List<Mark> findByExamId(UUID examId);
    List<Mark> findByStudentId(UUID studentId);
    List<Mark> findByExamIdAndStudentId(UUID examId, UUID studentId);
    Optional<Mark> findByExamIdAndStudentIdAndSubjectId(UUID examId, UUID studentId, UUID subjectId);
}
