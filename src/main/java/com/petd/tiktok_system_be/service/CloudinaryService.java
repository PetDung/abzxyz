package com.petd.tiktok_system_be.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-preset}")
    String uploadPreset;


    public String uploadFile(MultipartFile file) throws IOException {
        Map<String, Object> params = ObjectUtils.asMap(
                "upload_preset", uploadPreset,   // sử dụng preset bạn đã tạo
                "folder", "tiktok_design",       // có thể override folder
                "overwrite", true,
                "unique_filename", true
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult.get("secure_url").toString();
    }

    public boolean deleteByUrl(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                throw new IllegalArgumentException("URL không đúng định dạng Cloudinary");
            }
            String path = parts[1];
            path = path.replaceFirst("^v\\d+/", "");
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }
            String publicId = path;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Xóa thành công: " + publicId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
