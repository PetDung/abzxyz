package com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    @JsonProperty("label_link")
    private String labelLink;

    @JsonProperty("OrderId")
    private String orderId;

    private String seller;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("order_source")
    private String orderSource;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("AddressLine1")
    private String addressLine1;

    @JsonProperty("AddressLine2")
    private String addressLine2;

    @JsonProperty("City")
    private String city;

    @JsonProperty("StateOrRegion")
    private String stateOrRegion;

    @JsonProperty("Zip")
    private String zip;

    @JsonProperty("CountryCode")
    private String countryCode;

    private String phone;

    private List<OrderItemPrintRequest> items;

    @JsonProperty("product_service")
    @Builder.Default
    private String productService = "Standard";

    @JsonProperty("push_tracking")
    @Builder.Default
    private boolean pushTracking = false;

    @JsonProperty("business_date")
    @Builder.Default
    private String businessDate = LocalDateTime.now().toString();
}
