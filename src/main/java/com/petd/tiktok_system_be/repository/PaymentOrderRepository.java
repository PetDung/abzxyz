package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Order.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {
}
