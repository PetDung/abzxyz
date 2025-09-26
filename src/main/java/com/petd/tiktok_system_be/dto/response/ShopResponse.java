package com.petd.tiktok_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {
    String id;
    String tiktokShopName;
    String userShopName;
    LocalDateTime createdAt;
    String ownerName;
    List<String> productUpload;
}
