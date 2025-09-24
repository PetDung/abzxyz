package com.petd.tiktok_system_be.service.Product.productDetailsMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.petd.tiktok_system_be.api.body.productRequestUpload.ProductAttribute;
import com.petd.tiktok_system_be.api.body.productRequestUpload.SkuUpload;
import com.petd.tiktok_system_be.entity.Order.MainImage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailsMap {
    JsonNode audit;
    List<CategoryChain> categoryChains;
    Long createTime;
    String description;
    boolean hasDraft;
    String id;
    boolean isCodAllowed;
    boolean isNotForSale;
    boolean isPreOwned;
    boolean isReplicated;
    String listingQualityTier;
    List<MainImage> mainImages;
    List<String> manufacturerIds;
    PackageDimensions packageDimensions;
    PackageWeight packageWeight;
    List<ProductAttribute> productAttributes;
    String productStatus;
    List<JsonNode> recommendedCategories;
    List<String> responsiblePersonIds;
    String shippingInsuranceRequirement;

    SizeChartProductDetails sizeChart;

    List<SkuUpload> skus;
    String status;
    SubscribeInfo subscribeInfo;
    String title;
    Long updateTime;
} 