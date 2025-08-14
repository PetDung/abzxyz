package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Team;
import com.petd.tiktok_system_be.repository.AccountRepository;
import com.petd.tiktok_system_be.repository.TeamRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InitDataService {

    AccountRepository accountRepository;
    TeamRepository teamRepository;
    PasswordService passwordService;


    @Transactional
    public void initAccount() {

        String leaderId = "7bf0db0e-f9d3-497a-901b-efbc735d8fac";
        String password = passwordService.encodePassword("Dung1702@");

        if(accountRepository.existsById(leaderId)) return;

        Team team = Team.builder()
                .teamName("Default Team")
                .build();
        teamRepository.saveAndFlush(team);

        accountRepository.insertIfNotExists(
                leaderId,
                "Default leader",
                "0386117963",
                password,
                Role.Leader.toString(),
                team.getId(),
                true
        );

        log.info("Tạo account test thủ công bằng SQL với id cố định thành công!");
    }

}
