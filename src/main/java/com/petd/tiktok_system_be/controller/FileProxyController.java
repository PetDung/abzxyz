package com.petd.tiktok_system_be.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileProxyController {

    static final String CACHE_DIR = "cache/files";

    @GetMapping("/thumb")
    public ResponseEntity<StreamingResponseBody> getThumbnail(@RequestParam("id") String fileId) {
        try {
            Path cachePath = Paths.get(CACHE_DIR, fileId + "_thumb.jpg");
            Files.createDirectories(cachePath.getParent());

            // ✅ Nếu đã cache → trả luôn
            if (Files.exists(cachePath)) {
                log.info("Serve thumbnail from cache: {}", cachePath);
                StreamingResponseBody cachedStream = out -> {
                    try (InputStream cachedIn = Files.newInputStream(cachePath)) {
                        cachedIn.transferTo(out);
                    }
                };
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(cachedStream);
            }

            // ✅ Chưa cache → tải thumbnail từ Google Drive
            // Lấy thumbnail nhỏ hơn (s250)
            String thumbUrl = "https://lh3.googleusercontent.com/d/" + fileId + "=s250";
            URL url = new URL(thumbUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                return ResponseEntity.status(status)
                        .body(out -> out.write(("Drive error: " + status).getBytes()));
            }

            String contentType = connection.getContentType();

            // ✅ Trả trực tiếp và cache cùng lúc
            StreamingResponseBody stream = outputStream -> {
                try (InputStream in = connection.getInputStream()) {
                    try (OutputStream cacheOut = Files.newOutputStream(cachePath, StandardOpenOption.CREATE)) {
                        in.transferTo(new TeeOutputStream(outputStream, cacheOut));
                    }
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .contentType(MediaType.parseMediaType(
                            contentType != null ? contentType : "image/jpeg"))
                    .body(stream);

        } catch (Exception e) {
            log.error("Error while serving thumbnail {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(out -> out.write(("Lỗi khi tải thumbnail: " + e.getMessage()).getBytes()));
        }
    }

    static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out1.write(b, off, len);
            out2.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }

        @Override
        public void close() throws IOException {
            out1.close();
            out2.close();
        }
    }
}
