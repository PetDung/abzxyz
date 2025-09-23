package com.petd.tiktok_system_be.service.Return;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.Specification.ReturnSpecification;
import com.petd.tiktok_system_be.api.RefundApi;
import com.petd.tiktok_system_be.api.body.RefundBody;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Return.Return;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.ReturnRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.service.Manager.PrinterService;
import com.petd.tiktok_system_be.service.NotificationService;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReturnService {

    RequestClient requestClient;
    ShopService shopService;
    ReturnRepository returnRepository;

    public JsonNode getReturn (String shopId, Map<String, String> params, Integer pageSize) {

        String nextPageToken = params.get("next_page_token");
        String returnTypes = StringUtils.isNotBlank(params.get("return_types")) ? params.get("return_types") : null;

        Shop shop = shopService.getShopByShopId(shopId);

        String orderId = params.get("order_id");
        String returnId = params.get("return_id");

        List<String> orderIds = new ArrayList<>();
        List<String> returnIds = new ArrayList<>();

        if(StringUtils.isNotBlank(orderId)) orderIds.add(orderId);
        if(StringUtils.isNotBlank(returnId)) returnIds.add(returnId);


        try{
            RefundBody body  = RefundBody.builder()
                    .returnTypes(returnTypes)
                    .orderIds(orderIds)
                    .returnIds(returnIds)
                    .build();
            TiktokCallApi returnApi = RefundApi.builder()
                    .shopCipher(shop.getCipher())
                    .accessToken(shop.getAccessToken())
                    .pageToken(nextPageToken)
                    .pageSize(pageSize)
                    .body(body)
                    .requestClient(requestClient)
                    .build();

            TiktokApiResponse response = returnApi.callApi();
            return response.getData();

        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (Exception e) {
            throw new AppException(e.getMessage(), 409);
        }


    }

    public ResponsePage<Return> search(String keyword, Pageable pageable) {

        List<Shop> myShops = shopService.getMyShops();
        if (myShops == null || myShops.isEmpty()) {
            return new ResponsePage<>(
                    List.of(),
                    0,
                    0, // vì Page mặc định bắt đầu từ 0
                    true
            );
        }

        List<String> ids =  myShops.stream().map(Shop::getId).toList();

        Specification<Return> shopSpec = ReturnSpecification.hasShopIds(ids);

        Specification<Return> keywordSpec = Specification
                .where(ReturnSpecification.hasReturnId(keyword))
                .or(ReturnSpecification.hasOrderId(keyword));

        Specification<Return> spec = shopSpec.and(keywordSpec);

        Page<Return> pageResult = returnRepository.findAll(spec, pageable);

        return new ResponsePage<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getNumber(), // vì Page mặc định bắt đầu từ 0
                pageResult.isLast()
        );
    }
}
