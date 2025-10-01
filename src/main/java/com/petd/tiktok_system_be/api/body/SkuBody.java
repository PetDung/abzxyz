package com.petd.tiktok_system_be.api.body;

import com.petd.tiktok_system_be.api.body.productRequestUpload.Inventory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkuBody {
    String sku;
    List<Inventory> inventory;

}
