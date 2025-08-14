package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.ShopGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopGroupRepository extends JpaRepository<ShopGroup, Long> {


    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END
        FROM ShopGroup sg
        JOIN sg.employees e
        JOIN sg.groupShopAccess gsa
        JOIN gsa.shop s
        WHERE e.id = :employeeId
          AND s.id = :shopId
    """)
    boolean existsEmployeeHasAccessToShop(String employeeId, String shopId);
}
