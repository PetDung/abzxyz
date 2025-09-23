package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Auth.SettingSystem;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.repository.SettingSystemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GoogleAuthService {

    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    SettingRepository settingRepository;
    SettingSystemRepository systemRepository;

    public GoogleCredential getCredential(SettingSystem setting) throws GeneralSecurityException, IOException {
        String clientId = System.getenv("GG_CLIENT_ID");
        String clientSecret = System.getenv("GG_CLIENT_SECRET");

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setAccessToken(setting.getGgAccessToken())
                .setRefreshToken(setting.getGgRefreshToken());

        if (credential.getRefreshToken() != null) {
            if (credential.refreshToken()) {
                setting.setGgAccessToken(credential.getAccessToken());
                systemRepository.save(setting);
            } else {
                throw new RuntimeException("Cannot refresh Google access token");
            }
        }

        return credential;
    }
}
