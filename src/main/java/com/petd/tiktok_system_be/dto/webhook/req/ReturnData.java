package com.petd.tiktok_system_be.dto.webhook.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReturnData {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("return_role")
    private String returnRole;

    @JsonProperty("return_type")
    private String returnType;

    @JsonProperty("return_status")
    private String returnStatus;

    @JsonProperty("return_id")
    private String returnId;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("update_time")
    private Long updateTime;
}
