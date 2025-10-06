package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.util.CursorUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {



    @Query(value = """
        SELECT *
        FROM product p
        WHERE p.status IN (:status)
          AND (:productId IS NULL OR p.id = :productId)      
          AND p.shop_id IN (:shopIds)
          AND (:startDate IS NULL OR 
               (:filter = 'ACTIVE' AND p.active_time BETWEEN :startDate AND :endDate) OR
               (:filter = 'UPDATE' AND p.update_time BETWEEN :startDate AND :endDate))
          AND (:lastActiveTime IS NULL OR 
               (p.active_time < :lastActiveTime OR 
               (p.active_time = :lastActiveTime AND p.id < :lastProductId)))
        ORDER BY p.active_time DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findProductsWithCursor(
            @Param("status") List<String> status,
            @Param("productId") String productId,
            @Param("shopIds") List<String> shopIds,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate,
            @Param("filter") String filter,
            @Param("lastActiveTime") Long lastActiveTime,
            @Param("lastProductId") String lastProductId,
            @Param("limit") int limit
    );
    @Query(value = """
        SELECT COUNT(*)
        FROM product p
        WHERE p.status IN (:status)
          AND (:productId IS NULL OR p.id = :productId)           
          AND p.shop_id IN (:shopIds)
          AND (:startDate IS NULL OR 
               (:filter = 'ACTIVE' AND p.active_time BETWEEN :startDate AND :endDate) OR
               (:filter = 'UPDATE' AND p.update_time BETWEEN :startDate AND :endDate))
        """, nativeQuery = true)
    long countActiveProducts(
                @Param("status") List<String> status,
                @Param("productId") String productId,
                @Param("shopIds") List<String> shopIds,
                @Param("startDate") Long startDate,
                @Param("endDate") Long endDate,
                @Param("filter") String filter
    );

}
