package com.petd.tiktok_system_be.entity.Design;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Base;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import jakarta.persistence.*;
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
public class Design  extends Base {
    String name;
    String frontSide;
    String backSide;
    String leftSide;
    String rightSide;
    String thumbnail;

    @OneToMany(mappedBy = "design", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<OrderItem> orderItems;


    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonIgnore
    Account account;

    @OneToMany(mappedBy = "design", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MappingDesign> mappingDesigns = new ArrayList<>();


    @PreRemove
    private void preRemove() {
        for (OrderItem item : orderItems) {
            item.setDesign(null);
        }
    }
}
