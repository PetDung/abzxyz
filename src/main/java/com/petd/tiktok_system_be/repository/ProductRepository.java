package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
