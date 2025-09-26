package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.productRequestUpload.*;
import com.petd.tiktok_system_be.entity.Order.MainImage;
import com.petd.tiktok_system_be.service.Product.productDetailsMap.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReupProduct {

    ProductService productService;
    public ProductUpload copyProduct (String shopId, String productId){
        try {
            ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);;
            JsonNode productJson = productService.getProduct(shopId, productId);
            ProductDetailsMap  productDetailsMap =  mapper.convertValue(productJson, ProductDetailsMap.class);
            return convertToProductUpload(productDetailsMap, productId);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return null;
        }
    }
    public ProductUpload convertToProductUpload(ProductDetailsMap src, String productId){
        return ProductUpload.builder()
                .productOriginId(productId)
                .categoryId(getLastCategoryId(src.getCategoryChains()))
                .saveMode("LISTING")
                .description(src.getDescription())
                .title(src.getTitle())
                .isCodAllowed(src.isCodAllowed())
                .packageDimensions(getPackageDimensions(src.getPackageDimensions()))
                .packageWeight(getPackageWeightUpload(src.getPackageWeight()))
                .brandId("7186147431263340294")
                .categoryVersion("v2")
                .listingPlatforms(List.of("TIKTOK_SHOP"))
                .sizeChart(getSizeChartUploads(src.getSizeChart()))
                .mainImages(getMainImage(src.getMainImages()))
                .skus(src.getSkus())
                .video(null)
                .certifications(null)
                .externalProductId(null)
                .deliveryOptionIds(null)
                .isNotForSale(false)
                .isPreOwned(false)
                .manufacturerIds(null)
                .responsiblePersonIds(null)
                .shippingInsuranceRequirement(null)
                .minimumOrderQuantity(null)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }
    public String getLastCategoryId(List<CategoryChain> categoryChains) {
        if (categoryChains == null || categoryChains.isEmpty()) {
            throw new IllegalArgumentException("categoryChains is null or empty");
        }
        return categoryChains.get(categoryChains.size() - 1).getId();
    }

    public PackageDimensionsUpload getPackageDimensions(PackageDimensions src){
        return PackageDimensionsUpload.builder()
                .unit(src.getUnit())
                .width(src.getWidth())
                .height(src.getHeight())
                .length(src.getLength())
                .build();
    }
    public PackageWeightUpload getPackageWeightUpload(PackageWeight src){
        return PackageWeightUpload.builder()
                .unit(src.getUnit())
                .value(src.getValue())
                .build();
    }
    public SizeChart getSizeChartUploads(SizeChartProductDetails src){
        if(src == null) return null;
        String uri = src.getImage().getUri();
        Image image = Image.builder()
                .uri(uri)
                .urls(src.getImage().getUrls())
                .build();
        return SizeChart.builder()
                .image(image)
                .template(null)
                .build();
    }
    public List<Image> getMainImage (List<MainImage> src){
        return src.stream()
                .map(item -> Image.builder()
                        .urls(item.getUrls())
                        .build()
                )
                .toList();
    }

}
