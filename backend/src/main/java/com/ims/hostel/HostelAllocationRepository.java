package com.ims.hostel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, UUID> {
    Page<HostelAllocation> findByStatus(AllocationStatus status, Pageable pageable);
    Page<HostelAllocation> findByHostelId(UUID hostelId, Pageable pageable);
}
