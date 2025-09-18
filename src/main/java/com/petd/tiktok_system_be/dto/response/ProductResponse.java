package com.petd.tiktok_system_be.dto.response;

import com.petd.tiktok_system_be.entity.Product.Product;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    List<Product> products;
    long totalCount;
    long currentPage;
    boolean isLast;
}
