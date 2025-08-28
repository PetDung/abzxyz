package com.petd.tiktok_system_be.dto.message;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProductMessage {
    String shopId;
    List<String> productIds;
}
