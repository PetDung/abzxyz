package com.petd.tiktok_system_be.api.body.productRequestUpload;


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
public class Certification {
    private String id;
    private List<Image> images;
    private List<File> files;
    @JsonProperty("expiration_date")
    private Long expirationDate;
}
