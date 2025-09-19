package com.petd.tiktok_system_be.initalizer;

import com.petd.tiktok_system_be.service.InitDataService;
import com.petd.tiktok_system_be.service.Queue.OrderSyncService;
import com.petd.tiktok_system_be.service.Order.ShippingService;
import com.petd.tiktok_system_be.service.Queue.ReturnSync;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class DataInit  implements CommandLineRunner {

    InitDataService initDataService;
    OrderSyncService service;
    ShippingService shippingService;
    ReturnSync returnSync;

    @Override
    public void run(String... args) throws Exception {
        initDataService.initAccount();
        initDataService.initAccount2();
//        service.pubJobStatus("COMPLETED");
//        JsonNode shipping = shippingService.getShipping("577072234678489961", "7495997094249991038");
//        log.info("logs 7495997094249991038 {}", shipping.asText());
//          returnSync.pushJob();
    }
}
