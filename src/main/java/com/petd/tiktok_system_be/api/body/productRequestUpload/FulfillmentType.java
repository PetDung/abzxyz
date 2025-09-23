package com.petd.tiktok_system_be.api.body.productRequestUpload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FulfillmentType {
    @JsonProperty("handling_duration_days")
    private Integer handlingDurationDays;
    @JsonProperty("release_date")
    private Long releaseDate;
}
