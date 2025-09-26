package com.petd.tiktok_system_be.repository;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Product.UploadedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UploadProductRepository extends JpaRepository<UploadedProduct,String> {


    Optional<UploadedProduct> findByProductIdAndShop(String productId, Shop shop);

    boolean existsByProductIdAndShop(String productId, Shop shop);
}
