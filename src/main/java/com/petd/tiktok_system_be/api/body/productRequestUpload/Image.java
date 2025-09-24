package com.petd.tiktok_system_be.api.body.productRequestUpload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private String uri;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> urls;

}
