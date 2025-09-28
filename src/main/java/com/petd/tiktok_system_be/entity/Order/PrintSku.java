package com.petd.tiktok_system_be.entity.Order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.petd.tiktok_system_be.entity.Base;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(
        name = "print_sku",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sku_code", "print_code"})
        }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintSku extends Base {
    String skuCode;
    String type;
    String value1;
    String value2;
    String printCode;

    @OneToMany(mappedBy = "printSku")
    @JsonIgnore
    List<OrderItem> orderItems;
}
