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
public class ItemMPK {

    @JsonProperty("product_id")
    private String productId;
    private String qty;   // JSON mẫu có "qty": "2" nên để String. Nếu muốn int -> đổi đây.
    private String size;
    private String color;
    private List<DesignMKP> designs;
}
