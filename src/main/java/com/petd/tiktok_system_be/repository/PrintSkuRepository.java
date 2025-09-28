package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Order.PrintSku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrintSkuRepository extends JpaRepository<PrintSku, String> {
    Optional<PrintSku> findByPrintCodeAndSkuCode(String printCode, String skuCode);
}
