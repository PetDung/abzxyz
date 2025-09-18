package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Auth.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO account (id, name, user_name, password, role, team_id, is_active ) " +
                    "VALUES (:id, :name,:userName, :password, :role, :teamId, :isActive) " +
                    "ON CONFLICT (id) DO NOTHING", // PostgreSQL tránh lỗi trùng id
            nativeQuery = true
    )
    void insertIfNotExists(String id, String name, String userName, String password, String role, String teamId, boolean isActive);


    Optional<Account> findByUserName(String userName);


    @Query("select distinct e from Account e " +
            "join e.group g " +
            "join g.groupShopAccess ga " +
            "where ga.shop.id = :shopId")
    List<Account> findEmployeesByShopId(@Param("shopId") String shopId);

}
