package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query("""
        SELECT oi.productId AS productId, 
               oi.productName AS productName, 
               COUNT(oi) AS soldCount, 
               o.shop.userShopName AS shopName,
               o.shop.id as shopId, 
               MIN(oi.skuImage) AS skuImage
        FROM OrderItem oi
        JOIN oi.order o
        WHERE (:productId IS NULL OR oi.productId = :productId)
          AND (:shopIds IS NULL OR o.shop.id IN :shopIds)
          AND (:startDate IS NULL OR o.createTime >= :startDate)
          AND (:endDate IS NULL OR o.createTime <= :endDate)
        GROUP BY oi.productId, oi.productName, o.shop.userShopName, o.shop.id
        ORDER BY soldCount DESC
    """)
    List<Object[]> countProductSales(
            @Param("productId") String productId,
            @Param("shopIds") List<String> shopIds,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );

    @Query(value = """
    SELECT t.product_id, t.product_name, t.sold_count, t.shop_name, t.shop_id, t.sku_image
    FROM (
        SELECT 
            oi.product_id,
            oi.product_name,
            COUNT(*) AS sold_count,
            s.user_shop_name AS shop_name,
            s.id AS shop_id,
            MIN(oi.sku_image) AS sku_image
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        JOIN shop s ON o.shop_id = s.id
        WHERE (:productId IS NULL OR oi.product_id = :productId)
          AND (o.shop_id IN (:shopIds))
          AND (:startDate IS NULL OR o.create_time >= :startDate)
          AND (:endDate IS NULL OR o.create_time <= :endDate)
        GROUP BY oi.product_id, oi.product_name, s.user_shop_name, s.id
    ) t
    WHERE (:lastSoldCount IS NULL
           OR (t.sold_count < :lastSoldCount)
           OR (t.sold_count = :lastSoldCount AND t.product_id < :lastProductId))
    ORDER BY t.sold_count DESC, t.product_id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findProductSalesWithCursor(
            @Param("productId") String productId,
            @Param("shopIds") List<String> shopIds,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate,
            @Param("lastSoldCount") Long lastSoldCount,
            @Param("lastProductId") String lastProductId,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT COUNT(DISTINCT oi.product_id)
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE (:productId IS NULL OR oi.product_id = :productId)
          AND (o.shop_id IN (:shopIds))
          AND (:startDate IS NULL OR o.create_time >= :startDate)
          AND (:endDate IS NULL OR o.create_time <= :endDate)
        """, nativeQuery = true)
    Long countDistinctProducts(
            @Param("productId") String productId,
            @Param("shopIds") List<String> shopIds,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );




    List<OrderItem> findBySkuIdAndProductId(String skuId, String productId);
}
