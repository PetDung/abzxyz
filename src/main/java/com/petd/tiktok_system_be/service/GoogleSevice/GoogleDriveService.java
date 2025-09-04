package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.petd.tiktok_system_be.entity.Setting;
import com.petd.tiktok_system_be.repository.SettingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleDriveService {

    GoogleAuthService googleAuthService;
    SettingRepository settingRepository;

    public Drive getDriveService(Setting setting) throws Exception {
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                googleAuthService.getCredential(setting)
        )
                .setApplicationName("tiktok_system_be")
                .build();
    }


    public File uploadFileFromUrl(String fileUrl, String mimeType, String driveFolderId, String fileName) throws Exception {
        Setting setting = settingRepository.findAll().get(0);
        Drive drive = getDriveService(setting);

        // Lấy input stream từ URL
        InputStream inputStream = getPdfFromUrl(fileUrl);
        InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

        // Tìm file đã tồn tại trong folder theo tên
        String query = "name = '" + fileName.replace("'", "\\'") + "'";
        if (driveFolderId != null) {
            query += " and '" + driveFolderId + "' in parents";
        }
        query += " and trashed = false";

        FileList result = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            // File đã tồn tại → update
            String fileId = result.getFiles().get(0).getId();
            return drive.files().update(fileId, null, mediaContent)
                    .setFields("id, name, webViewLink, webContentLink")
                    .execute();
        } else {
            // File chưa tồn tại → tạo mới
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (driveFolderId != null) {
                fileMetadata.setParents(Collections.singletonList(driveFolderId));
            }
            return drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, webViewLink, webContentLink")
                    .execute();
        }
    }

    private InputStream getPdfFromUrl(String fileUrl) throws Exception {
        return new URL(fileUrl).openStream();
    }
}
