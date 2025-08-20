package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
