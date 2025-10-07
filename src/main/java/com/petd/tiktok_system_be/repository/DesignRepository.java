package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Design.Design;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DesignRepository extends JpaRepository<Design, String> {
//
//    Optional<Design> findByProductIdAndSkuId(String productId, String skuId);
//
//    boolean existsByProductIdAndSkuId(String productId, String skuId);

    List<Design> findAllByAccount_Id(String accountId);

    @Query(value = """
    SELECT *
    FROM design d
    WHERE d.is_active = true
      AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE CONCAT('%', LOWER(:keyword), '%'))
      AND (:lastCreatedAt IS NULL OR 
           ((EXTRACT(EPOCH FROM d.created_at) * 1000) < :lastCreatedAt OR 
            ((EXTRACT(EPOCH FROM d.created_at) * 1000) = :lastCreatedAt AND d.id < :lastId)))
    ORDER BY d.created_at DESC, d.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Design> findAllWithCursor(
            @Param("keyword") String keyword,
            @Param("lastCreatedAt") Long lastCreatedAt,  // UTC millis
            @Param("lastId") String lastId,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT *
    FROM design d
    WHERE d.is_active = true
      AND d.account_id = :accountId
      AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE CONCAT('%', LOWER(:keyword), '%'))
      AND (:lastCreatedAt IS NULL OR 
           ((EXTRACT(EPOCH FROM d.created_at) * 1000) < :lastCreatedAt OR 
            ((EXTRACT(EPOCH FROM d.created_at) * 1000) = :lastCreatedAt AND d.id < :lastId)))
    ORDER BY d.created_at DESC, d.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Design> findAllByAccountWithCursor(
            @Param("accountId") String accountId,
            @Param("keyword") String keyword,
            @Param("lastCreatedAt") Long lastCreatedAt,  // UTC millis
            @Param("lastId") String lastId,
            @Param("limit") int limit
    );



    @Query(value = """
    SELECT COUNT(*)
    FROM design d
    WHERE d.is_active = true
      AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE CONCAT('%', LOWER(:keyword), '%'))
    """, nativeQuery = true)
    long countAllWithCursor(@Param("keyword") String keyword);


    @Query(value = """
    SELECT COUNT(*)
    FROM design d
    WHERE d.is_active = true
      AND d.account_id = :accountId
      AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE CONCAT('%', LOWER(:keyword), '%'))
    """, nativeQuery = true)
    long countAllByAccountWithCursor(
            @Param("accountId") String accountId,
            @Param("keyword") String keyword
    );


}
