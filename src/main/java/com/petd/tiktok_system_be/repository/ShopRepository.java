package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, String> {


    List<Shop> findByLeader_Id(String leader_id);

    boolean existsByLeaderAndId(Account leader, String id);

    @Query("""
        SELECT s
        FROM Shop s
        JOIN GroupShopAccess gsa ON gsa.shop = s
        JOIN gsa.group sg
        JOIN sg.employees e
        WHERE e.id = :accountId
    """)
    List<Shop> findByAccountGroupAccess(@Param("accountId") String accountId);
}
