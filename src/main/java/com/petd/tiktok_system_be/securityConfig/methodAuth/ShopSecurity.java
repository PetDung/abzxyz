package com.petd.tiktok_system_be.securityConfig.methodAuth;


import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Group.ShopGroup;
import com.petd.tiktok_system_be.service.Auth.AccountService;
import com.petd.tiktok_system_be.service.Manager.GroupService;
import com.petd.tiktok_system_be.service.Shop.ShopService;
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
