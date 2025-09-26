package com.petd.tiktok_system_be.api.body.productRequestUpload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductUpload {

    @JsonIgnore
    String productOriginId;

    @JsonProperty("save_mode")
    private String saveMode;

    private String description;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("brand_id")
    private String brandId;

    @JsonProperty("main_images")
    private List<Image> mainImages;

    private List<SkuUpload> skus;
    private String title;

    @JsonProperty("is_cod_allowed")
    private Boolean isCodAllowed;

    private List<Certification> certifications;

    @JsonProperty("package_dimensions")
    private PackageDimensionsUpload packageDimensions;

    @JsonProperty("product_attributes")
    private List<ProductAttribute> productAttributes;

    @JsonProperty("package_weight")
    private PackageWeightUpload packageWeight;

    private Video video;

    @JsonProperty("external_product_id")
    private String externalProductId;

    @JsonProperty("delivery_option_ids")
    private List<String> deliveryOptionIds;

    @JsonProperty("size_chart")
    private SizeChart sizeChart;

    @JsonProperty("primary_combined_product_id")
    private String primaryCombinedProductId;

    @JsonProperty("is_not_for_sale")
    private Boolean isNotForSale;

    @JsonProperty("category_version")
    private String categoryVersion;

    @JsonProperty("manufacturer_ids")
    private List<String> manufacturerIds;

    @JsonProperty("responsible_person_ids")
    private List<String> responsiblePersonIds;

    @JsonProperty("listing_platforms")
    private List<String> listingPlatforms;

    @JsonProperty("shipping_insurance_requirement")
    private String shippingInsuranceRequirement;

    @JsonProperty("minimum_order_quantity")
    private Integer minimumOrderQuantity;

    @JsonProperty("is_pre_owned")
    private Boolean isPreOwned;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;
}
