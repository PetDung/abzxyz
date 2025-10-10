package com.petd.tiktok_system_be.service.GoogleSevice;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

@Service
public class FileFetcherService {

    public InputStream fromUrl(String fileUrl) throws Exception {
        return new URL(fileUrl).openStream();
    }

    public InputStream fromLocal(String filePath) throws Exception {
        return new FileInputStream(filePath);
    }

    // sau này có thể thêm fromBytes(byte[] data) nếu cần
}
