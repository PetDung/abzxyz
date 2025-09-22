package com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.request;
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
    String platformShipping;
    String seller;
    String address1;
    String address2;
    String city;
    String postCode;
    String phone;
    String state;
    String country;
    String lastName;
    String firstName;
    List<ProductOption> productOptions;
    String urlLabel;
    String trackingId;
    String idOrder;
    String methodShipping;
    String note;
}
