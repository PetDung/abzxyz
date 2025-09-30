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
public class DesignMKP {
    @JsonProperty("side_name")
    private String sideName;
    private List<String> images;
}
