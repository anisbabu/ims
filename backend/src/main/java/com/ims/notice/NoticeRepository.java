package com.ims.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoticeRepository extends JpaRepository<Notice, UUID> {
    Page<Notice> findByTitleContainingIgnoreCase(String q, Pageable pageable);
}
