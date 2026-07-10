package com.ims.transport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransportAssignmentRepository extends JpaRepository<TransportAssignment, UUID> {
    Page<TransportAssignment> findByStatus(AssignmentStatus status, Pageable pageable);
    Page<TransportAssignment> findByRouteId(UUID routeId, Pageable pageable);
}
