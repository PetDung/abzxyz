package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Design;
import com.petd.tiktok_system_be.entity.MappingDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MappingDesignRepository extends JpaRepository<MappingDesign, String> {

    @Query(value = """
    SELECT * 
    FROM mapping_design md
    WHERE md.product_id = :productId
      AND md.skus @> to_jsonb(:skuId)
    """, nativeQuery = true)
    Optional<MappingDesign> findByProductIdAndSku(@Param("productId") String productId,
                                                  @Param("skuId") String skuId);

    Optional<MappingDesign> findByProductIdAndDesign(String productId, Design design);
}
