package com.petd.tiktok_system_be.initalizer;

import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.entity.Account;
import com.petd.tiktok_system_be.entity.Team;
import com.petd.tiktok_system_be.repository.AccountRepository;
import com.petd.tiktok_system_be.repository.TeamRepository;
import com.petd.tiktok_system_be.service.InitDataService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class DataInit  implements CommandLineRunner {

    InitDataService initDataService;

    @Override
    public void run(String... args) throws Exception {
        initDataService.initAccount();
        initDataService.initAccount2();
    }
}
