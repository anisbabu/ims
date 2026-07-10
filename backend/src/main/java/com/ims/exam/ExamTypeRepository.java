package com.ims.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamTypeRepository extends JpaRepository<ExamType, UUID> {
}
