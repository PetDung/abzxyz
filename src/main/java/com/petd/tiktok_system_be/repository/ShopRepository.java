package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    Page<Shop> findByLeader_Id(String leaderId, Pageable pageable);

    // Phân trang theo group access
    @Query("""
        SELECT s
        FROM Shop s
        JOIN GroupShopAccess gsa ON gsa.shop = s
        JOIN gsa.group sg
        JOIN sg.employees e
        WHERE e.id = :accountId
    """)
    Page<Shop> findByAccountGroupAccess(@Param("accountId") String accountId, Pageable pageable);

    Optional<Shop> findByUserShopName(String userShopName);

    List<Shop> findByAccessTokenExpiryLessThan(Long expiryTime);


}
