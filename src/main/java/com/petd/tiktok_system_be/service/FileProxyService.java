package com.petd.tiktok_system_be.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileProxyService {

    public MultipartFile downloadFileAsMultipart(String fileId, String size) {
        try {
            // URL Google Drive chuẩn
            String thumbUrl = "https://lh3.googleusercontent.com/d/" + fileId + "=s250";

            URL url = new URL(thumbUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Google Drive error: " + status);
            }

            String contentType = connection.getContentType();
            String fileName = fileId + "_" + size + ".jpg";

            try (InputStream in = connection.getInputStream()) {
                return new InputStreamMultipartFile(fileName, fileName, contentType, in);
            }

        } catch (Exception e) {
            log.error("Lỗi khi tải file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Không tải được file từ Google Drive", e);
        }
    }
}
