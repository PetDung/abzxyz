package com.petd.tiktok_system_be.api.body;

import lombok.*;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadProductImage {
    File file;
    String use_case;
}
