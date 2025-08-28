package com.petd.tiktok_system_be.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginSuccessResponse {
    String id;
    String username;
    String name;
    String role;
    String team;
    String accessToken;
}
