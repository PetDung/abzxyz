package com.petd.tiktok_system_be.api.body;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dimension {
    String unit;
    String length;
    String width;
    String height;
}
