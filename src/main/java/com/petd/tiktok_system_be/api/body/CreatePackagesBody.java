package com.petd.tiktok_system_be.api.body;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePackagesBody {
    List<String> orderLineItemIds;
    String orderId;
    String shippingServiceId;
    Weight weight;
    Dimension dimension;
}
