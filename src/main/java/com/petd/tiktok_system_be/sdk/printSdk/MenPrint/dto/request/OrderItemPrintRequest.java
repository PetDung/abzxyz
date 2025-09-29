package com.petd.tiktok_system_be.sdk.printSdk.MenPrint.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemPrintRequest {
    private String sku;
    private int quantity;

    @JsonProperty("front_url")
    private String frontUrl;

    @JsonProperty("mockup_front_url")
    private String mockupFrontUrl;

    @JsonProperty("back_url")
    private String backUrl;

    @JsonProperty("mockup_back_url")
    @Builder.Default
    private String mockupBackUrl="";

    @JsonProperty("left_sleeve")
    @Builder.Default
    private String leftSleeve="";

    @JsonProperty("right_sleeve")
    @Builder.Default
    private String rightSleeve="";

    @Builder.Default
    private String note="";

    @JsonProperty("special_front_url")
    @Builder.Default
    private String specialFrontUrl="";

    @JsonProperty("special_back_url")
    @Builder.Default
    private String specialBackUrl="";

    @JsonProperty("special_left_sleeve")
    @Builder.Default
    private String specialLeftSleeve="";

    @JsonProperty("special_right_sleeve")
    @Builder.Default
    private String specialRightSleeve= "";

    @JsonProperty("mockup_special_front_url")
    @Builder.Default
    private String mockupSpecialFrontUrl="";

    @JsonProperty("mockup_special_back_url")
    @Builder.Default
    private String mockupSpecialBackUrl="";

    @JsonProperty("mockup_special_left_sleeve")
    @Builder.Default
    private String mockupSpecialLeftSleeve="";

    @JsonProperty("mockup_special_right_sleeve")
    @Builder.Default
    private String mockupSpecialRightSleeve= "";

    @JsonProperty("print_size_front")
    @Builder.Default
    private String printSizeFront="14x16";

    @JsonProperty("print_size_back")
    @Builder.Default
    private String printSizeBack="14x16";

    @JsonProperty("print_tech")
    @Builder.Default
    private String printTech="DTG Print";

}
