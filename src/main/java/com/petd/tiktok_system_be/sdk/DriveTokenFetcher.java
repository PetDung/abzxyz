package com.petd.tiktok_system_be.sdk;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.petd.tiktok_system_be.entity.Setting;
import com.petd.tiktok_system_be.repository.SettingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DriveTokenFetcher {

    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    SettingRepository settingRepository;
    String path   = System.getenv("GG_CERTIFICATE");
    public void getTokenGG() throws Exception {

        System.out.println(path);

        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = new FileInputStream(path);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList("https://www.googleapis.com/auth/drive.file")
        )
                .setAccessType("offline") // lấy refresh token
                .build();

        // Open browser để login
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");


        System.out.println("access token: " + credential.getAccessToken());
        System.out.println("refresh token: " + credential.getRefreshToken());

        Setting setting;

        List<Setting> settings = settingRepository.findAll();

        if(settings.isEmpty()){
            setting = new Setting();
        }else {
            setting = settings.get(0);
        }
        setting.setGgAccessToken(credential.getAccessToken());
        setting.setGgRefreshToken(credential.getRefreshToken());
        settingRepository.save(setting);

    }
}
