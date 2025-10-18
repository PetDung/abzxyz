package com.petd.tiktok_system_be.sdk.printSdk.mango.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    private List<Insert> inserts;
    private List<FileInfo> preview_files;
    private List<FileInfo> print_files;
    private String production_config;
    private int quantity;
    private String sku;
}
