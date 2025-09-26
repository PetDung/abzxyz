package com.petd.tiktok_system_be.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Base;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "uploaded_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"productId", "shopId"})
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadedProduct extends Base {

    String productId;

    @ManyToOne
    @JoinColumn(name = "shopId")
    @JsonIgnore
    Shop shop;

}
