package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.dto.request.LoginRequest;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.util.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    PasswordService passwordService;
    AccountService accountService;
    JwtUtils jwtUtils;

    public LoginSuccessResponse login(LoginRequest loginRequest, HttpServletResponse response) throws Exception {

        Account accountLogin = accountService.getAccountByUserName(loginRequest.getUsername());
        if(!passwordService.matches(loginRequest.getPassword(), accountLogin.getPassword())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        String token = jwtUtils.generateToken(accountLogin.getUsername());

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true) // HTTPS
                .path("/")
                .sameSite("None") // có sẵn, không cần thủ công
                .maxAge(Duration.ofMillis(jwtUtils.getExpiration()))
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return LoginSuccessResponse.builder()
                .id(accountLogin.getId())
                .name(accountLogin.getName())
                .role(accountLogin.getRole())
                .team(accountLogin.getTeam().getTeamName())
                .username(accountLogin.getUsername())
                .accessToken(token)
                .build();
    }
}
