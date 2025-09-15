package com.petd.tiktok_system_be.dto.response;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingResponse {
    String orderSheetId;
    String connectUrl;
    String driverId;
}
