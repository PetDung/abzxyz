package com.petd.tiktok_system_be.entity.Group;

import com.petd.tiktok_system_be.entity.Base;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "group_shop_access",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "shop_id"})
        }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupShopAccess extends Base {

    @ManyToOne
    @JoinColumn(name = "group_id")
    ShopGroup group;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    Shop shop;

}
