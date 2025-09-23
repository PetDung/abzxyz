package com.petd.tiktok_system_be.service.Auth;
import com.petd.tiktok_system_be.dto.response.SettingResponse;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Auth.SettingSystem;
import com.petd.tiktok_system_be.repository.AccountRepository;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.repository.SettingSystemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SettingService {

    SettingRepository settingRepository;
    AccountService accountService;
    AccountRepository accountRepository;
    SettingSystemRepository settingSystemRepository;

    public SettingResponse getSettingResponse (){
        Account account = accountService.getMe();
        Optional<Setting> optionalSetting = settingRepository.findByAccount_Id(account.getId());

        Setting setting = optionalSetting.orElseGet(() -> {
            Setting newSetting = new Setting();
            newSetting.setAccount(account);
            account.setSetting(newSetting);
            accountRepository.save(account);
            return newSetting;
        });

        SettingSystem settingSystem = settingSystemRepository.findAll().get(0);
        return SettingResponse.builder()
                .connectUrl(settingSystem.getConnectUrl())
                .driverId(setting.getDriverId())
                .orderSheetId(setting.getOrderSheetId())
                .build();
    }

    public Setting getSetting (){
        Account account = accountService.getMe();
        Optional<Setting> optionalSetting = settingRepository.findByAccount_Id(account.getId());
        return optionalSetting.orElseGet(() -> {
            Setting newSetting = new Setting();
            newSetting.setAccount(account);
            account.setSetting(newSetting);
            accountRepository.save(account);
            return newSetting;
        });
    }

    public Setting getSetting (Account account){
        Optional<Setting> optionalSetting = settingRepository.findByAccount_Id(account.getId());
        return optionalSetting.orElseGet(() -> {
            Setting newSetting = new Setting();
            newSetting.setAccount(account);
            account.setSetting(newSetting);
            accountRepository.save(account);
            return newSetting;
        });
    }
}
