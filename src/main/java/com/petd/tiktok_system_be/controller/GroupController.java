package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.request.GroupRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.GroupResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.dto.response.ShopResponse;
import com.petd.tiktok_system_be.service.Manager.GroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GroupController {

    GroupService groupService;


    @PreAuthorize("hasAuthority('Leader')")
    @PostMapping
    public ApiResponse<GroupResponse> addGroup(@RequestBody GroupRequest request) {
        return ApiResponse.<GroupResponse>builder()
                .result(groupService.createGroup(request))
                .build();
    }

    @PreAuthorize("hasAuthority('Leader')")
    @GetMapping
    public ApiResponse<List<GroupResponse>> getGroup() {
        return ApiResponse.<List<GroupResponse>>builder()
                .result(groupService.getMyGroups())
                .build();
    }

    @PreAuthorize("@shopSecurity.isAcceptGroup(#groupId)")
    @GetMapping("/member/{groupId}")
    public ApiResponse<List<LoginSuccessResponse>> getMember(@PathVariable("groupId") String groupId) {
        return ApiResponse.<List<LoginSuccessResponse>>builder()
                .result(groupService.getAccountByGroupId(groupId))
                .build();
    }

    @PreAuthorize("@shopSecurity.isAcceptGroup(#groupId)")
    @GetMapping("/shop-member/{groupId}")
    public ApiResponse<List<ShopResponse>> getShopMember(@PathVariable("groupId") String groupId) {
        return ApiResponse.<List<ShopResponse>>builder()
                .result(groupService.getShopByGroupId(groupId))
                .build();
    }
    @PreAuthorize("@shopSecurity.isAccept(#shopId) and @shopSecurity.isAcceptGroup(#groupId)")
    @PutMapping("/update/shop-member/{groupId}/{shopId}")
    public ApiResponse<GroupResponse> updateShopMember(
            @PathVariable("groupId") String groupId,
            @PathVariable String shopId,
            @RequestParam (defaultValue = "REMOVE") String action) {
        return ApiResponse.<GroupResponse>builder()
                .result(groupService.updateShopToGroup(groupId, shopId, action))
                .build();
    }
    @PreAuthorize("@shopSecurity.isAcceptGroup(#groupId)")
    @PutMapping("/update/member/{groupId}/{userId}")
    public ApiResponse<GroupResponse> updateMember(
            @PathVariable("groupId") String groupId,
            @PathVariable String userId,
            @RequestParam (defaultValue = "REMOVE") String action) {
        return ApiResponse.<GroupResponse>builder()
                .result(groupService.updateMemberToGroup(groupId, userId, action))
                .build();
    }

}
