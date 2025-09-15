package com.petd.tiktok_system_be.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.dto.request.UpdateProfileRequest;
import com.petd.tiktok_system_be.dto.request.UserRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    AccountService accountService;

    @PreAuthorize("hasAuthority('Leader')")
    @GetMapping("/member")
    public ApiResponse<List<LoginSuccessResponse>> get () throws JsonProcessingException {
        return ApiResponse.<List<LoginSuccessResponse>>builder()
                .result(accountService.getMember())
                .build();
    }
    @PreAuthorize("hasAuthority('Leader')")
    @PostMapping("/create-employee")
    public ApiResponse<LoginSuccessResponse> createEmployee (@RequestBody UserRequest userRequest)  {
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(accountService.createEmployee(userRequest))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<LoginSuccessResponse> getMe ()  {
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(accountService.getMeResponse())
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<LoginSuccessResponse> updatePersonalInfo(
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(accountService.updatePersonalInfo(request))
                .build();
    }
}
