package com.petd.tiktok_system_be.entity.Order;

import com.petd.tiktok_system_be.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "print_shipping_method",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "print_code"})
        }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintShippingMethod extends Base {
    String type;
    String printCode;
}
