package com.petd.tiktok_system_be.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GoogleDriveService {


    public InputStream getPdfFromUrl(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        return url.openStream(); // stream trực tiếp từ URL
    }
}
