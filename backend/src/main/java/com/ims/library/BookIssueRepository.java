package com.ims.library;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookIssueRepository extends JpaRepository<BookIssue, UUID> {
    Page<BookIssue> findByStatus(BookIssueStatus status, Pageable pageable);
    Page<BookIssue> findByStudentId(UUID studentId, Pageable pageable);
}
