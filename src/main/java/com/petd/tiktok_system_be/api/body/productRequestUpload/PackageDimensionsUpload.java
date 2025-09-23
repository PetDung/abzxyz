package com.petd.tiktok_system_be.api.body.productRequestUpload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDimensions {
    private String length;
    private String width;
    private String height;
    private String unit;
}

