package com.petd.tiktok_system_be.api.body.productRequestUpload;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {
    private String id;
    private String name;
    private String format;

}
