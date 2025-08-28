package com.petd.tiktok_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingResponse {
    private Dimension dimension;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_line_id")
    private List<String> orderLineId;

    @JsonProperty("shipping_services")
    private List<ShippingService> shippingServices;

    private Weight weight;

    @Data
    public static class Dimension {
        private String height;
        private String length;
        private String unit;
        private String width;
    }

    @Data
    public static class ShippingService {
        private String currency;

        @JsonProperty("earliest_delivery_days")
        private int earliestDeliveryDays;

        private String id;

        @JsonProperty("is_default")
        private boolean isDefault;

        @JsonProperty("latest_delivery_days")
        private int latestDeliveryDays;

        private String name;
        private String  price;

        @JsonProperty("shipping_provider_id")
        private String shippingProviderId;

        @JsonProperty("shipping_provider_name")
        private String shippingProviderName;
    }

    @Data
    public static class Weight {
        private String unit;
        private String value;
    }
}
