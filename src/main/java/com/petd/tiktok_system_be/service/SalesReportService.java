package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.dto.response.ProductReportResponse;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.repository.OrderItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SalesReportService {

    OrderItemRepository orderItemRepository;
    ShopService shopService;

    public ProductReportResponse getProductSales(String productId, String productName, Long startDate, Long endDate) {

        Instant nowUtc = Instant.now(); // thời gian UTC hiện tại

        if (startDate == null) {
            startDate = nowUtc.minus(30, ChronoUnit.DAYS).getEpochSecond();
        }
        if (endDate == null) {
            endDate = nowUtc.getEpochSecond();
        }

        List<Shop> myShops = shopService.getMyShops();
        List<String> shopIds = myShops.stream()
                .map(Shop::getId)
                .toList();

        List<Object[]> results = orderItemRepository.countProductSales(productId, shopIds, startDate, endDate);

        List<ProductSalesDTO> list = results.stream().map(obj -> {
            String pid = (String) obj[0];
            String name = (String) obj[1];
            Long soldCount = (Long) obj[2];
            String shopName = (String) obj[3];
            return new ProductSalesDTO(pid, name, soldCount, shopName);
        }).collect(Collectors.toList());

        return ProductReportResponse.builder()
                .products(list)
                .endEnd(endDate)
                .startDate(startDate)
                .build();
    }
    // DTO để trả về
    public record ProductSalesDTO(
            String productId,
            String productName,
            Long soldCount,
            String shopName
    ) {}
}
