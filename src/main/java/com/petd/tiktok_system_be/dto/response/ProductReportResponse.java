package com.petd.tiktok_system_be.dto.response;

import com.petd.tiktok_system_be.service.SalesReportService;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductReportResponse {

    List<SalesReportService.ProductSalesDTO> products;
    Long startDate;
    Long endEnd;
}
