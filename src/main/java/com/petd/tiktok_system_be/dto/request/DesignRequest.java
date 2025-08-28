package com.petd.tiktok_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DesignRequest {
    String name;
    String frontSide;
    String backSide;
    String leftSide;
    String rightSide;
}
