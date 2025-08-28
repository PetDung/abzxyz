package com.petd.tiktok_system_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopGroup  extends Base{

    @Column(nullable = false)
    String groupName;


    @OneToMany(mappedBy = "group")
    @Builder.Default
    List<GroupShopAccess> groupShopAccess = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    @Builder.Default
    List<Account> employees = new ArrayList<>();

}
