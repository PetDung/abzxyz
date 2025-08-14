package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.dto.request.LoginRequest;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge( (int) jwtUtils.getExpiration()/ 1000);

        response.addCookie(cookie);

        return LoginSuccessResponse.builder()
                .id(accountLogin.getId())
                .name(accountLogin.getName())
                .role(accountLogin.getRole())
                .team(accountLogin.getTeam().getTeamName())
                .username(accountLogin.getUsername())
                .build();
    }
}
