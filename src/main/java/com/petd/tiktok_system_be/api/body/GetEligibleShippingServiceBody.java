package com.petd.tiktok_system_be.api.body;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEligibleShippingServiceBody {
    List<String> orderLineItemIds;
    Weight weight;
    Dimension dimension;
}
