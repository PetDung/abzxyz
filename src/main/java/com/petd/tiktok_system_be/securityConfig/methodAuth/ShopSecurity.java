package com.petd.tiktok_system_be.securityConfig.methodAuth;


import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.entity.ShopGroup;
import com.petd.tiktok_system_be.service.AccountService;
import com.petd.tiktok_system_be.service.GroupService;
import com.petd.tiktok_system_be.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopSecurity {


    AccountService accountService;
    ShopService shopService;
    GroupService groupService;

    public boolean isAccept(String shopId){
        Account account = accountService.getMe();
        return  shopService.checkShopBelongUser(account.getId(), shopId);
    }

    public boolean isAcceptGroup(String groupId){
        Account account = accountService.getMe();
        ShopGroup shopGroup =  groupService.getById(groupId);
        log.info("{}", account.getId().equals(shopGroup.getLeader().getId()));
        return account.getId().equals(shopGroup.getLeader().getId());
    }



}
