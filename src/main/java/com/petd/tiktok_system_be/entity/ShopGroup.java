package com.petd.tiktok_system_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ShopGroup  extends Base{

    @Column(nullable = false)
    String groupName;

    String description;


    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL,  orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    List<GroupShopAccess> groupShopAccess = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    List<Account> employees = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "leader_id")
    @JsonIgnore
    Account leader;

    @Transient
    Integer memberCount;

    @Transient
    Integer shopCount;

    @Transient
    public Integer getShopCount (){
        return groupShopAccess.size();
    }
    @Transient
    public Integer getMemberCount (){
        return employees.size();
    }

}
