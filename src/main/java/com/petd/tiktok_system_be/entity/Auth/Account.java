package com.petd.tiktok_system_be.entity.Auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petd.tiktok_system_be.entity.Base;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Group.ShopGroup;
import com.petd.tiktok_system_be.entity.Manager.Team;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account extends Base implements UserDetails {

    @Column(nullable = false, unique = true)
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

    @OneToOne
    @JoinColumn(name = "my_team_id")
    @JsonIgnore
    Team myTeam;

    @ManyToOne
    @JoinColumn(name = "group_id")
    ShopGroup group;


    @OneToOne(cascade = CascadeType.ALL) // tự động persist, merge, remove entity liên quan
    @JoinColumn(name = "setting_id")
    @JsonIgnore
    Setting setting;


    @OneToMany(mappedBy = "account",  cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Design> designs;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return this.userName;
    }
}
