package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.dto.request.GroupRequest;
import com.petd.tiktok_system_be.dto.response.GroupResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.dto.response.ShopResponse;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.GroupShopAccess;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.entity.ShopGroup;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.mapper.GroupMapper;
import com.petd.tiktok_system_be.mapper.ShopMapper;
import com.petd.tiktok_system_be.repository.AccountRepository;
import com.petd.tiktok_system_be.repository.ShopGroupRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GroupService {

    ShopGroupRepository shopGroupRepository;
    AccountService accountService;
    GroupMapper  groupMapper;
    ShopMapper shopMapper;
    ShopService shopService;
    AccountRepository accountRepository;

    public ShopGroup getById(String id){
        return  shopGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    public GroupResponse createGroup(GroupRequest request) {
        if(request.getGroupName() == null || request.getGroupName().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }
        Account account = accountService.getMe();
        ShopGroup shopGroup = ShopGroup.builder()
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .leader(account)
                .build();
        return groupMapper.toGroupResponse(shopGroupRepository.save(shopGroup));
    }

    public List<GroupResponse> getMyGroups() {
        Account account = accountService.getMe();
        List<ShopGroup>  list= new ArrayList<>();
        if(account.getRole().equals(Role.Leader.toString())){
            list = shopGroupRepository.findAllByLeaderId(account.getId());
        }else if(account.getRole().equals(Role.Admin.toString())){
            list = shopGroupRepository.findAll();
        }
        return groupMapper.toGroupResponseList(list);
    }

    public List<LoginSuccessResponse> getAccountByGroupId(String groupId) {
        ShopGroup shopGroup = getById(groupId);

        List<Account> list = shopGroup.getEmployees();

        return list.stream().map((item) -> LoginSuccessResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .role(item.getRole())
                .role(item.getRole())
                .username(item.getUsername())
                .build()).toList();
    }

    public List<ShopResponse> getShopByGroupId(String groupId) {
        ShopGroup shopGroup =  getById(groupId);
        List<GroupShopAccess> list = shopGroup.getGroupShopAccess();
        return list.stream().map((item) ->shopMapper.toShopResponse(item.getShop())).toList();
    }
    public GroupResponse updateShopToGroup(String groupId, String shopId, String action) {
        ShopGroup shopGroup = getById(groupId); // lấy group luôn từ DB
        Shop shop = shopService.getShopByShopId(shopId);
        if ("ADD".equalsIgnoreCase(action)) {
            boolean exists = shopGroup.getGroupShopAccess().stream()
                    .anyMatch(access -> access.getShop().getId().equals(shop.getId()));
            if (!exists) {
                GroupShopAccess groupShopAccess = GroupShopAccess.builder()
                        .group(shopGroup)
                        .shop(shop)
                        .build();
                shopGroup.getGroupShopAccess().add(groupShopAccess);
            }
        } else if ("REMOVE".equalsIgnoreCase(action)) {
            shopGroup.getGroupShopAccess().removeIf(
                    access -> access.getShop().getId().equals(shop.getId())
            );
        }
        return groupMapper.toGroupResponse(shopGroupRepository.save(shopGroup));
    }

    public GroupResponse updateMemberToGroup(String groupId, String userId, String action) {
        ShopGroup shopGroup = getById(groupId); // lấy group luôn từ DB
        Account account = accountService.getById(userId);
        if ("ADD".equalsIgnoreCase(action)) {
            boolean exists = shopGroup.getEmployees().stream()
                    .anyMatch(access -> access.getId().equals(account.getId()));
            if (!exists) {
                shopGroup.getEmployees().add(account);
                account.setGroup(shopGroup);
            }
        } else if ("REMOVE".equalsIgnoreCase(action)) {
            shopGroup.getEmployees().removeIf(
                    access -> access.getId().equals(account.getId())
            );
            account.setGroup(null);
        }
        return groupMapper.toGroupResponse(shopGroupRepository.save(shopGroup));
    }
}
