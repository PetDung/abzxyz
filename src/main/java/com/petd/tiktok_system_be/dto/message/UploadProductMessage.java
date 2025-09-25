package com.petd.tiktok_system_be.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.petd.tiktok_system_be.api.body.productRequestUpload.ProductUpload;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadProductMessage {
    String shopId;
    ProductUpload productUpload;
}
