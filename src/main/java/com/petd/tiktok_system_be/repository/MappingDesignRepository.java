package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Design.MappingDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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


    @Query(value = """
    SELECT md.design_id, elem AS matched_sku
    FROM mapping_design md
    JOIN jsonb_array_elements_text(md.skus) elem ON TRUE
    WHERE md.product_id = :productId
      AND elem = ANY(:skuIds)
    """, nativeQuery = true)
    List<Object[]> findDesignsByProductIdAndSkuIds(
            @Param("productId") String productId,
            @Param("skuIds") String[] skuIds
    );


    Optional<MappingDesign> findByProductIdAndDesign(String productId, Design design);

    List<MappingDesign> findByProductId(String productId);
}
