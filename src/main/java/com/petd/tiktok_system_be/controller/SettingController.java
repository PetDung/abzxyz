package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.dto.response.SettingResponse;
import com.petd.tiktok_system_be.service.Auth.SettingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setting")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SettingController {

    SettingService settingService;

    @GetMapping
    public ApiResponse<SettingResponse> getSettingRepository() {
        return  ApiResponse.<SettingResponse>builder()
                .result(settingService.getSettingResponse())
                .build();
    }

//    @PutMapping("/label")
//    public ApiResponse<Setting> autoBuyLabelUpdate(AutoGetLabelRequest request) {
//        Setting setting;
//        List<Setting> settings = settingRepository.findAll();
//
//        if(settings.isEmpty()){
//            setting = new Setting();
//        }else {
//            setting = settings.get(0);
//        }
//        return  ApiResponse.<Setting>builder()
//                .result(setting)
//                .build();
//    }
}
