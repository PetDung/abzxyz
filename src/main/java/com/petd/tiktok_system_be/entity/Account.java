package com.petd.tiktok_system_be.entity;

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
public class Account extends Base{

    @Column(nullable = false)
    String userName;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    String role;

    @Column(nullable = false)
    String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    Team team;

    @ManyToOne
    @JoinColumn(name = "group_id")
    ShopGroup group;


    @Transient
    List<Shop> shops = new ArrayList<>();



}
