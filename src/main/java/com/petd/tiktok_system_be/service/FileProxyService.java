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

    // Class custom MultipartFile, không cần thư viện ngoài
    private static class InputStreamMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public InputStreamMultipartFile(String name, String originalFilename, String contentType, InputStream inputStream) throws Exception {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = inputStream.readAllBytes();
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return originalFilename; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(File dest) throws FileNotFoundException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
