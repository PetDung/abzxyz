package com.petd.tiktok_system_be.api.body;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestBody {
  private String orderStatus;
  private Long createTimeGe;
  private Long createTimeLt;
  private Long updateTimeGe;
  private Long updateTimeLt;
  private String shippingType;
  private String buyerUserId;
  private Boolean isBuyerRequestCancel;
  private List<String> warehouseIds;
}
