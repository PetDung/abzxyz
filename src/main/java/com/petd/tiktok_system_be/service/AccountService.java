package com.petd.tiktok_system_be.service;


import com.petd.tiktok_system_be.constant.Role;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService {

    AccountRepository accountRepository;


    public Account getAccountByUserName(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    public Account getById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<Account> getAllAccountsAccessShop(Shop shop) {
        List<Account> list = accountRepository.findEmployeesByShopId(shop.getId());
        list.add(shop.getLeader());
        return list;
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
        return (Account) authentication.getPrincipal();
    }
}
