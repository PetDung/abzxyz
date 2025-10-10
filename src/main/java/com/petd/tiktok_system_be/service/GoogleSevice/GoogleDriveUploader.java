package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleDriveUploader {

    GoogleAuthService googleAuthService;

    public File uploadOrUpdate(InputStream inputStream, String mimeType, String folderId, String fileName)
            throws Exception {

        Drive drive = googleAuthService.getDriveService();
        InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

        // Tìm file trùng tên
        String query = "name = '" + fileName.replace("'", "\\'") + "'";
        if (folderId != null) query += " and '" + folderId + "' in parents";
        query += " and trashed = false";

        FileList result = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            // Update file cũ
            String fileId = result.getFiles().get(0).getId();
            return drive.files().update(fileId, null, mediaContent)
                    .setFields("id, name, webViewLink, webContentLink")
                    .execute();
        } else {
            // Upload file mới
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (folderId != null) {
                fileMetadata.setParents(Collections.singletonList(folderId));
            }
            return drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, webViewLink, webContentLink")
                    .execute();
        }
    }
}
