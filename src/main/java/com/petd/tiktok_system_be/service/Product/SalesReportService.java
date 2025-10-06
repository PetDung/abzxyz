package com.petd.tiktok_system_be.service.Product;

import com.petd.tiktok_system_be.dto.response.ProductReportResponse;
import com.petd.tiktok_system_be.dto_v2.response.CursorPageResponse;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.OrderItemRepository;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import com.petd.tiktok_system_be.util.CursorUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
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
            String shopId = (String) obj[4];
            String skuImage = (String) obj[5];
            ShopSalesDTO shopSalesDTO = new ShopSalesDTO(shopName, shopId);
            return new ProductSalesDTO(pid, name, soldCount, shopSalesDTO, skuImage);
        }).collect(Collectors.toList());

        return ProductReportResponse.builder()
                .products(list)
                .endEnd(endDate)
                .startDate(startDate)
                .build();
    }


    public CursorPageResponse<ProductSalesDTO> getProductSalesCursor(
            String productId,
            Long startDate,
            Long endDate,
            String cursor
    ) {
        log.info(productId);
        if (productId != null && productId.isBlank()) productId = null;

        if (productId != null) {
            startDate = null;
            endDate = null;
        } else {
            // nếu không có productId, set default 30 ngày
            Instant nowUtc = Instant.now();
            if (startDate == null) startDate = nowUtc.minus(30, ChronoUnit.DAYS).getEpochSecond();
            if (endDate == null) endDate = nowUtc.getEpochSecond();
        }

        // lấy shopIds của user
        List<Shop> myShops = shopService.getMyShops();
        List<String> shopIds = myShops.stream()
                .map(Shop::getId)
                .toList();

        // decode cursor nếu có
        Long lastSoldCount = null;
        String lastProductId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.Cursor decoded = CursorUtils.decodeCursor(cursor);
            String[] values = decoded.value().split("\\|");
            lastSoldCount = Long.parseLong(values[0]);
            lastProductId = values[1];
        }

        int pageSize = 10;

        // fetch dữ liệu (pageSize + 1 để kiểm tra hasMore)
        List<Object[]> rows = orderItemRepository.findProductSalesWithCursor(
                productId,
                shopIds,
                startDate,
                endDate,
                lastSoldCount,
                lastProductId,
                pageSize + 1
        );

        // map về DTO
        List<ProductSalesDTO> list = rows.stream()
                .limit(pageSize)
                .map(obj -> {
                    String pid = (String) obj[0];
                    String name = (String) obj[1];
                    Long soldCount = (Long) obj[2];
                    String shopName = (String) obj[3];
                    String shopId = (String) obj[4];
                    String skuImage = (String) obj[5];
                    return new ProductSalesDTO(pid, name, soldCount, new ShopSalesDTO(shopName, shopId), skuImage);
                })
                .toList();

        Long total = orderItemRepository.countDistinctProducts(
                productId,
                shopIds,
                startDate,
                endDate
        );

        // build cursor tiếp theo
        String nextCursor = null;
        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            ProductSalesDTO last = list.get(list.size() - 1);
            String cursorValue = last.soldCount + "|" + last.productId;
            nextCursor = CursorUtils.encodeCursor("soldCount,productId", cursorValue);
        }

        // trả về response
        return CursorPageResponse.<ProductSalesDTO>builder()
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .data(list)
                .total(total)
                .build();
    }

    // DTO để trả về
    public record ProductSalesDTO(
            String productId,
            String productName,
            Long soldCount,
            ShopSalesDTO shop,
            String skuImage
    ) {}

    public record ShopSalesDTO(
            String shopName,
            String shopId
    ) {}
}
