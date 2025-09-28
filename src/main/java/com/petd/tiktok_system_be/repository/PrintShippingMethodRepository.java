package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Order.PrintShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintShippingMethodRepository extends JpaRepository<PrintShippingMethod, String> {
    List<PrintShippingMethod> findAllShippingMethodsByPrintCode(String printCode);
}
