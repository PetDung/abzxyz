package com.petd.tiktok_system_be.service.PrintCase;

import com.petd.tiktok_system_be.Specification.OrderSpecification;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.PrintShippingMethod;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.PrintShippingMethodRepository;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GetPrintOrder {

    ShopService shopService;
    OrderRepository orderRepository;
    PrintShippingMethodRepository printShippingMethodRepository;

    public ResponsePage<Order> getOrderCanPrint( String orderId, List<String> shopIds,  Integer page, List<String> printStatus ) {
        List<String> status = List.of("AWAITING_COLLECTION", "AWAITING_SHIPMENT");
        return getAllOrderOnDataBaseByOwnerId(
                orderId,
                shopIds,
                status,
                page,
                printStatus
        );
    }
    private ResponsePage<Order> getAllOrderOnDataBaseByOwnerId(
            String orderId,
            List<String> shopIds,
            List<String> status,
            Integer page,
            List<String> printStatus
    ) {
        List<Shop> myShops = shopService.getMyShops();

        if(myShops == null || myShops.isEmpty()) {
            return ResponsePage.<Order>builder()
                    .data(new ArrayList<>())
                    .totalCount(0)
                    .currentPage(0)
                    .isLast(true)
                    .build();
        }
        // Ném lỗi nếu shopIds chứa id không hợp lệ
        if(hasInvalidShopId(myShops, shopIds)){
            throw new AppException(ErrorCode.FI);
        }

        List<String> id;
        if(shopIds != null && !shopIds.isEmpty()){
            id = shopIds;
        } else {
            id = myShops.stream().map(Shop::getId).collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(page, 20, Sort.by("createTime").descending());
        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.filterOrders(orderId, id, status, null, printStatus),
                pageable
        );

        return ResponsePage.<Order>builder()
                .data(orderPage.getContent())
                .totalCount(orderPage.getTotalElements())
                .currentPage(orderPage.getNumber())
                .isLast(orderPage.isLast())
                .build();
    }

    public List<PrintShippingMethod> getAll() {
        return printShippingMethodRepository.findAll();
    }


    private boolean hasInvalidShopId(List<Shop> myShops, List<String> shopIds) {
        if(shopIds == null || shopIds.isEmpty()) return false;
        Set<String> myShopIdSet = myShops.stream()
                .map(Shop::getId)
                .collect(Collectors.toSet());
        return shopIds.stream().anyMatch(id -> !myShopIdSet.contains(id));
    }

}
