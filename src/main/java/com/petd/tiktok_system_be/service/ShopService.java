package com.petd.tiktok_system_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.api.AuthorizedApi;
import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.dto.request.AuthShopRequest;
import com.petd.tiktok_system_be.dto.response.AuthShopResponse;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.ShopGroupRepository;
import com.petd.tiktok_system_be.repository.ShopRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.shared.TiktokAuthAppClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopService {


    TiktokAuthAppClient authClient;
    RequestClient requestClient;

    AccountService accountService;
    ShopRepository shopRepository;
    ShopGroupRepository shopGroupRepository;


    public Shop getShopByShopId(String shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
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


    public AuthShopResponse connectShop(AuthShopRequest request){
        try {

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


}
