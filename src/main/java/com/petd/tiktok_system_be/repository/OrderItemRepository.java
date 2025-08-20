package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
