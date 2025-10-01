package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    Page<Order> findAll(Pageable pageable);
    List<Order> findByIdIn(List<String> ids);

    List<Order> findAllByStatus(String status);

    @Query(value = """
        SELECT COALESCE(SUM(s.revenue_amount), 0)
        FROM orders o
        JOIN settlement s ON o.id = s.order_id
        LEFT JOIN returns r ON o.id = r.order_id
        WHERE o.status = 'COMPLETED'
          AND r.id IS NULL
          AND EXTRACT(YEAR FROM TO_TIMESTAMP(o.create_time)) = :year
          AND EXTRACT(MONTH FROM TO_TIMESTAMP(o.create_time)) = :month
        """, nativeQuery = true)
    BigDecimal getTotalRevenueCompleted(@Param("year") int year, @Param("month") int month);
}
