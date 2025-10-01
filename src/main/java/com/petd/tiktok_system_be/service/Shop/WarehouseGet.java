package com.petd.tiktok_system_be.service.Shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.Warehouse;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WarehouseGet {

    RequestClient requestClient;
    ShopRepository repository;
    TelegramService telegramService;


    // Hàm xử lý cho 1 shop
    public void updateWarehouseForShop(Shop shop) {
        Warehouse warehouse = new Warehouse();
        warehouse.setAccessToken(shop.getAccessToken());
        warehouse.setShopCipher(shop.getCipher());
        warehouse.setRequestClient(requestClient);
        try {
            TiktokApiResponse response = warehouse.callApi();
            JsonNode root = response.getData();
            JsonNode warehouses = root.get("warehouses");

            if (warehouses != null && warehouses.isArray()) {
                for (JsonNode w : warehouses) {
                    if ("SALES_WAREHOUSE".equals(w.path("type").asText())) {
                        String id = w.path("id").asText();
                        shop.setWarehouse(id);
                        repository.save(shop);
                        telegramService.sendMessage("Oke wh " + shop.getUserShopName());
                        log.info("Updated warehouse for shop: {} -> {}", shop.getUserShopName(), id);
                        break; // chỉ cần 1 id
                    }
                }
            } else {
                log.warn("Warehouses is not array or null for shop {}", shop.getUserShopName());
            }
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi cập nhật warehouse cho shop {}: {}", shop.getUserShopName(), e.getMessage());
        }
    }

    // Hàm xử lý cho toàn bộ list shop
    public void updateWarehouseForAllShops() {
        List<Shop> shops = repository.findAll();
        shops.forEach(this::updateWarehouseForShop);
        telegramService.sendMessage("Done!");
    }
}
