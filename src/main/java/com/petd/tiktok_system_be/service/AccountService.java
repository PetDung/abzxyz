package com.petd.tiktok_system_be.service;


import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService {

    AccountRepository accountRepository;



    public Account admin(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!account.getRole().equals(Role.Admin.toString())){
            throw new AppException(ErrorCode.FI);
        }
        return account;
    }

    public Account leader(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!account.getRole().equals(Role.Leader.toString())){
            throw new AppException(ErrorCode.FI);
        }
        return account;
    }
}
