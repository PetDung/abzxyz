package com.petd.tiktok_system_be.service.ExportConfig;

import com.petd.tiktok_system_be.entity.Design;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.OrderItem;
import com.petd.tiktok_system_be.service.DesignService;
import com.petd.tiktok_system_be.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Export {

    OrderService orderService;
    DesignService designService;

    public List<OrderExport> run(String  orderId){
        Order order = orderService.getById(orderId);
        List<OrderExport> list = buildExports(order);
        return list;
    }

    public List<OrderExport> buildExports(Order order) {
        return order.getLineItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getSkuId))
                .values()
                .stream()
                .map(items -> {
                    OrderItem first = items.get(0); // lấy item đầu tiên làm đại diện
                    int quantity = items.size();    // số lượng của skuId này
                    Design design = designService.getDesignBySkuIdAnhProductId(first.getSkuId(), first.getProductId());
                    return new OrderExport(order, quantity, first, design);
                })
                .toList();
    }

}
