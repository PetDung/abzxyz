package com.petd.tiktok_system_be.api.body;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundBody {
    List<String> returnIds;
    List<String> orderIds;
    String returnTypes;
}
