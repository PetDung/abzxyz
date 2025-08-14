package com.petd.tiktok_system_be.controller;
import com.petd.tiktok_system_be.dto.request.LoginRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<LoginSuccessResponse> login(@RequestBody LoginRequest request, HttpServletResponse response)
            throws Exception {
        log.info("Login Request: {}, {}", request.getUsername(), request.getPassword());
        return ApiResponse.<LoginSuccessResponse>builder()
                .result(authenticationService.login(request, response))
                .build();

    }

}
