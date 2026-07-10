package com.ims.transport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, UUID> {
}
