package com.petd.tiktok_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupResponse {
    String id;
    String groupName;
    String description;
    Boolean autoGetLabel;
    Boolean active;
    Integer memberCount;
    Integer shopCount;
}
