package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query("""
        SELECT oi.productId AS productId, 
               oi.productName AS productName, 
               COUNT(oi) AS soldCount, 
               o.shop.userShopName AS shopName
        FROM OrderItem oi
        JOIN oi.order o
        WHERE (:productId IS NULL OR oi.productId = :productId)
          AND (:shopIds IS NULL OR o.shop.id IN :shopIds)
          AND (:startDate IS NULL OR o.createTime >= :startDate)
          AND (:endDate IS NULL OR o.createTime <= :endDate)
        GROUP BY oi.productId, oi.productName, o.shop.userShopName
        ORDER BY soldCount DESC
    """)
    List<Object[]> countProductSales(
            @Param("productId") String productId,
            @Param("shopIds") List<String> shopIds,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );
}
