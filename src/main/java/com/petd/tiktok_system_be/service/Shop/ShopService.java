package com.petd.tiktok_system_be.service.Shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.AuthorizedApi;
import com.petd.tiktok_system_be.api.body.Event;
import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.dto.message.OrderSyncMessage;
import com.petd.tiktok_system_be.dto.message.WebhookMessage;
import com.petd.tiktok_system_be.dto.request.AuthShopRequest;
import com.petd.tiktok_system_be.dto.response.AuthShopResponse;
import com.petd.tiktok_system_be.dto.response.ResponsePage;
import com.petd.tiktok_system_be.dto.response.ShopResponse;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Auth.MappingUserSystem;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Auth.SettingSystem;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.mapper.ShopMapper;
import com.petd.tiktok_system_be.repository.*;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.service.Auth.AccountService;
import com.petd.tiktok_system_be.shared.TiktokAuthAppClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopService {

    TiktokAuthAppClient authClient;
    RequestClient requestClient;
    ShopMapper shopMapper;

    AccountService accountService;
    ShopRepository shopRepository;
    ShopGroupRepository shopGroupRepository;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper mapper = new ObjectMapper();
    SettingSystemRepository settingSystemRepository;

    public Shop getShopByShopId(String shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }
    public Shop getShopByShopName(String shopName) {
        return shopRepository.findByUserShopName(shopName)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    public void deleteShopByShopId(String shopId) {
        shopRepository.deleteById(shopId);
    }

    public ShopResponse update(Shop shopRequest) {
        if(shopRequest.getId() == null){
            throw new AppException(ErrorCode.RQ);
        }
        if(shopRequest.getUserShopName() == null || shopRequest.getUserShopName().isEmpty()){
            throw new AppException(ErrorCode.RQ);
        }
        Shop shop = getShopByShopId(shopRequest.getId());
        shop.setUserShopName(shopRequest.getUserShopName());
        shopRepository.save(shop);
        return shopMapper.toShopResponse(shop);
    }

    public Page<Shop> getMyShops(Account account, Pageable pageable) {
        if (account.getRole().equals(Role.Admin.toString())) {
            return shopRepository.findAll(pageable);
        }
        if (account.getRole().equals(Role.Leader.toString())) {
            return shopRepository.findByLeader_Id(account.getId(), pageable);
        }
        return shopRepository.findByAccountGroupAccess(account.getId(), pageable);
    }

    public List<Shop> getMyShops (Account account) {
        if(account.getRole().equals(Role.Admin.toString())){
            return  shopRepository.findAll();
        }
        if(account.getRole().equals(Role.Leader.toString())){
            return  shopRepository.findByLeader_Id(account.getId());
        }
       return shopRepository.findByAccountGroupAccess(account.getId());
    }

    public List<Shop> getMyShops () {
        Account account = accountService.getMe();
        if(account.getRole().equals(Role.Admin.toString())){
            return  shopRepository.findAll();
        }
        if(account.getRole().equals(Role.Leader.toString())){
            return  shopRepository.findByLeader_Id(account.getId());
        }
        return shopRepository.findByAccountGroupAccess(account.getId());
    }

    public ResponsePage<ShopResponse> getMyShopPage(int pageNumber, int pageSize) {
        Account account = accountService.getMe();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Shop> shopPage;

        if (account.getRole().equals(Role.Admin.toString())) {
            shopPage = shopRepository.findAll(pageable);
        } else if (account.getRole().equals(Role.Leader.toString())) {
            shopPage = shopRepository.findByLeader_Id(account.getId(), pageable);
        } else {
            shopPage = shopRepository.findByAccountGroupAccess(account.getId(), pageable);
        }

        return mapToResponsePage(shopPage);
    }

    private ResponsePage<ShopResponse> mapToResponsePage(Page<Shop> shopPage) {
        List<ShopResponse> shopResponseList = shopMapper.toShopResponseList(shopPage.getContent());
        return ResponsePage.<ShopResponse>builder()
                .currentPage(shopPage.getNumber())
                .totalCount(shopPage.getTotalElements())
                .isLast(shopPage.isLast())
                .orders(shopResponseList)
                .build();
    }


    public List<ShopResponse> getMyShopsResponse () {
        Account account = accountService.getMe();
        List<Shop> shops = getMyShops(account);
        return shopMapper.toShopResponseList(shops);
    }

    public boolean checkShopBelongUser(String accountId, String shopId) {

        Account account =  accountService.getById(accountId);

        if(account.getRole().equals(Role.Admin.toString())){
            return true;
        }

        if(account.getRole().equals(Role.Leader.toString())){
            return shopRepository.existsByLeaderAndId(account, shopId);
        }

        return shopGroupRepository.existsEmployeeHasAccessToShop(accountId, shopId);
    }

    MappingUserSystemRepository mappingUserSystemRepository;

    public AuthShopResponse connectShopSystem(AuthShopRequest request) {
        MappingUserSystem mappingUserSystem = mappingUserSystemRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String api = mappingUserSystem.getApi();
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<AuthShopResponse> response = restTemplate.postForEntity(
                    api,
                    request,
                    AuthShopResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Parse body lỗi từ server (nếu có)
            String errorBody = ex.getResponseBodyAsString();
            throw new AppException(4009, errorBody);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public AuthShopResponse connectShop(AuthShopRequest request){
        try {

            Optional<Shop> exitShop = shopRepository.findByUserShopName(request.getUserShopName());
            if(exitShop.isPresent()){
                throw new AppException(ErrorCode.EXIST_AL);
            }

            Account account = accountService.leader(request.getUserName());

            TiktokApiResponse response = authClient.getToken(request.getCode());
            JsonNode jsonNode = response.getData();
            if(jsonNode == null || !jsonNode.has("access_token")) {
                log.error("Auth response missing access_token: {}", response);
                throw new AppException("Invalid auth response",500);
            }

            String accessToken = jsonNode.get("access_token").asText();

            AuthorizedApi authorizedApi = AuthorizedApi.builder()
                    .requestClient(requestClient)
                    .accessToken(accessToken)
                    .build();

            TiktokApiResponse shopInfo = authorizedApi.callApi();
            JsonNode shopInfoJsonNode = shopInfo.getData().path("shops");
            log.info("Shop Info: {}", shopInfoJsonNode);

            String refreshToken = jsonNode.get("refresh_token").asText();
            Long accessTokenExpiry = Long.parseLong(jsonNode.get("access_token_expire_in").asText());

            String cipher = "";
            String id = null;
            String tiktokShopName = "" ;

            if (shopInfoJsonNode.isArray() && !shopInfoJsonNode.isEmpty()) {
                JsonNode firstShop = shopInfoJsonNode.get(0);
                tiktokShopName = firstShop.path("name").asText();
                cipher =  firstShop.path("cipher").asText();
                id = firstShop.path("id").asText();
            }

            if(id == null) throw new AppException("Lỗi hệ thống", 500);

            Shop shop = Shop.builder()
                    .id(id)
                    .tiktokShopName(tiktokShopName)
                    .cipher(cipher)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(accessTokenExpiry)
                    .leader(account)
                    .userShopName(request.getUserShopName())
                    .build();

            shopRepository.save(shop);

            OrderSyncMessage msg = OrderSyncMessage.builder()
                    .shopId(shop.getId())
                    .limit(10)
                    .build();

            SettingSystem setting = settingSystemRepository.findAll().get(0);

            Event eventOrder = Event.builder()
                    .address(setting.getOrderWebhook())
                    .event_type("PRODUCT_STATUS_CHANGE")
                    .build();
            Event eventProduct = Event.builder()
                    .address(setting.getProductWebhook())
                    .event_type("ORDER_STATUS_CHANGE")
                    .build();

            WebhookMessage orderWB  = WebhookMessage.builder()
                    .event(eventOrder)
                    .shopId(shop.getId())
                    .build();
            WebhookMessage productWB  = WebhookMessage.builder()
                    .event(eventProduct)
                    .shopId(shop.getId())
                    .build();

            kafkaTemplate.send("order-sync", shop.getId(), mapper.writeValueAsString(msg));
            kafkaTemplate.send("web-hook", shop.getId(), mapper.writeValueAsString(orderWB));
            kafkaTemplate.send("web-hook", shop.getId(), mapper.writeValueAsString(productWB));

            System.out.println("Pushed job for shop: " + shop.getId());

            return AuthShopResponse.builder()
                    .id(shop.getId())
                    .userShopName(shop.getUserShopName())
                    .build();

        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), 409);
        }
    }

    public List<Shop> getShopsNearExpiry(long thresholdInHours) {
        long now = System.currentTimeMillis();
        long threshold = now + thresholdInHours * 3600 * 1000; // giờ → millis
        return shopRepository.findByAccessTokenExpiryLessThan(threshold);
    }
}
