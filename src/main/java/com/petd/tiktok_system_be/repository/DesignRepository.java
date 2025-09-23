package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Design.Design;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignRepository extends JpaRepository<Design, String> {
//
//    Optional<Design> findByProductIdAndSkuId(String productId, String skuId);
//
//    boolean existsByProductIdAndSkuId(String productId, String skuId);

    List<Design> findAllByAccount_Id(String accountId);
}
