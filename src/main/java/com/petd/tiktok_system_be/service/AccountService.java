package com.petd.tiktok_system_be.service;


import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.dto.request.UpdateProfileRequest;
import com.petd.tiktok_system_be.dto.request.UserRequest;
import com.petd.tiktok_system_be.dto.response.LoginSuccessResponse;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService {

    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;


    public Account getAccountByUserName(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    public Account getById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<LoginSuccessResponse> getMember() {
        Account account = getMe();
        List<Account> members = account.getTeam().getMembers();
        return members.stream()
                .filter(i -> i.getRole().equals(Role.Employee.toString()))
                .map((item) -> LoginSuccessResponse.builder()
                        .role(item.getRole())
                        .id(item.getId())
                        .name(item.getName())
                        .username(item.getUsername())
                        .build()
                )
                .toList();
    }


    public List<Account> getAllAccountsAccessShop(Shop shop) {
        List<Account> list = accountRepository.findEmployeesByShopId(shop.getId());
        list.add(shop.getLeader());
        return list;
    }

    public LoginSuccessResponse createEmployee(UserRequest request) {
        Account account = createAccount(request, Role.Employee);
        return LoginSuccessResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .username(account.getUsername())
                .role(account.getRole())
                .team(account.getTeam().getTeamName())
                .build();
    }

    public Account createAccount(UserRequest request, Role role) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new AppException(ErrorCode.RQ);
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }
        accountRepository.findByUserName(request.getUsername())
                .ifPresent(acc -> {
                    throw new AppException(ErrorCode.EXIST_AL);
                });
        Account account = getMe();

        return accountRepository.save(
                Account.builder()
                        .team(account.getTeam())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .name(request.getName())
                        .userName(request.getUsername())
                        .role(role.toString())
                        .build()
        );
    }


    public Account admin(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!account.getRole().equals(Role.Admin.toString())){
            throw new AppException(ErrorCode.FI);
        }
        return account;
    }

    public Account leader(String username) {
        Account account = accountRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!account.getRole().equals(Role.Leader.toString())){
            throw new AppException(ErrorCode.FI);
        }
        return account;
    }

    public Account getMe(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        Account principal = (Account) authentication.getPrincipal();
        return getById(principal.getId());
    }

    public LoginSuccessResponse getMeResponse() {

        Account principal = getMe();
        return LoginSuccessResponse.builder()
                .id(principal.getId())
                .name(principal.getName())
                .username(principal.getUsername())
                .role(principal.getRole())
                .team(principal.getTeam().getTeamName())
                .build();
    }

    @Transactional
    public LoginSuccessResponse updatePersonalInfo(UpdateProfileRequest request) {
        Account account = getMe();

        // Cập nhật tên hiển thị
        if (request.getName() != null && !request.getName().trim().isEmpty()
                && !request.getName().equals(account.getName())) {
            account.setName(request.getName().trim());
        }

        // Đổi mật khẩu nếu có yêu cầu
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                throw new AppException(ErrorCode.RQ); // yêu cầu nhập mật khẩu hiện tại
            }

            if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_INVALID);
            }

            if (request.getNewPassword().length() < 6) {
                throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
            }
            account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        Account saved = accountRepository.save(account);

        return LoginSuccessResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .username(saved.getUsername())
                .role(saved.getRole())
                .team(saved.getTeam().getTeamName())
                .build();
    }
}
