package com.petd.tiktok_system_be.entity.Auth;

import com.petd.tiktok_system_be.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
@Entity
@Table(name = "setting_system")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SettingSystem extends Base {
    String orderWebhook;
    String productWebhook;
    String refundWebhook;
    String connectUrl;
    String ggAccessToken;
    String ggRefreshToken;
}
