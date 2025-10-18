package com.petd.tiktok_system_be.sdk.printSdk.mango.dto.request;

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
public class OrderRequest {
    private String address_line_1;
    private String address_line_2;
    private String city;
    private String country;
    private String email;
    private String facility;
    private String first_name;
    private List<Item> items;
    private String label_url;
    private String last_name;
    private String note;
    private String order_id;
    private String phone;
    private String seller;
    private String shipping_method;
    private String speed_type;
    private String state;
    private String zip;
}
