package com.petd.tiktok_system_be.service.Auth;
import com.petd.tiktok_system_be.dto.response.SettingResponse;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.repository.SettingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SettingService {

    SettingRepository settingRepository;

    public SettingResponse getSettingResponse (){
        Setting setting;
        List<Setting> settings = settingRepository.findAll();
        if(settings.isEmpty()){
            setting = new Setting();
        }else {
            setting = settings.get(0);
        }
        return SettingResponse.builder()
                .connectUrl(setting.getConnectUrl())
                .driverId(setting.getDriverId())
                .orderSheetId(setting.getOrderSheetId())
                .build();
    }

    public Setting getSetting (){
        Setting setting;
        List<Setting> settings = settingRepository.findAll();
        if(settings.isEmpty()){
            setting = new Setting();
        }else {
            setting = settings.get(0);
        }
        return setting;
    }
}
