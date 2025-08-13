package com.petd.tiktok_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "group_employee_access",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "employee_id"})
        }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupEmployeeAccess extends Base{

    @ManyToOne
    @JoinColumn(name = "group_id")
    ShopGroup group;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Account employee;
}
