package com.petd.tiktok_system_be.sdk.printSdk.Monkeykingprint.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderData {

    private String firstname;
    private String lastname;
    private String address1;
    private String address2;
    private String city;
    private String region;
    private String postcode;
    @JsonProperty("country_id")
    private String countryId;
    private String telephone;
    @JsonProperty("seller_order_id")
    private String sellerOrderId;
    @JsonProperty("shipping_method")
    private String shippingMethod;
    @JsonProperty("prepaid_label")
    private String prepaidLabel;
    @JsonProperty("shipment_id")
    private String shipmentId;
    private List<ItemMPK> items;
}
