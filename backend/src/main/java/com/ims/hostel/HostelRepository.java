package com.ims.hostel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HostelRepository extends JpaRepository<Hostel, UUID> {
}
