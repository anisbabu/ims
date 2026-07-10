package com.ims.fee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OnlinePaymentRepository extends JpaRepository<OnlinePayment, UUID> {
}
