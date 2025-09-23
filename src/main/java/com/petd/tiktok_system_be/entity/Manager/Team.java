package com.petd.tiktok_system_be.entity.Manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class Team  extends Base {

    String teamName;
    @OneToMany(mappedBy = "team")
    List<Account> members;

    @OneToOne(mappedBy = "myTeam")
    @JsonIgnore
    Account leader;

}
