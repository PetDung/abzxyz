package com.petd.tiktok_system_be.entity.Manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Base;
import com.petd.tiktok_system_be.entity.Order.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Printer  extends Base {
    String name;
    String description;
    String code;
    @OneToMany(mappedBy = "printer")
    @JsonIgnore
    List<Order> orders;
}
