package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.api.body.productRequestUpload.*;
import com.petd.tiktok_system_be.entity.Order.MainImage;
import com.petd.tiktok_system_be.repository.ProductRepository;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.service.Product.productDetailsMap.*;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReupProduct {

    ShopService shopService;
    ProductRepository productRepository;
    ProductService productService;
    RequestClient requestClient;


    public ProductDetailsMap copyProduct (String shopId, String productId){
        try {
            ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);;
            JsonNode productJson = productService.getProduct(shopId, productId);
            ProductDetailsMap  productDetailsMap =  mapper.convertValue(productJson, ProductDetailsMap.class);

            ProductUpload productUpload = new ProductUpload();

            ImageProductSize image = productDetailsMap.getSizeChart().getImage();
            System.out.println(image.getHeight());
            System.out.println(image.getWidth());

            productUpload.setSaveMode("LISTING");


            return productDetailsMap;
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return null;
        }
    }


    public ProductUpload convertToProductUpload(ProductDetailsMap src){

        return ProductUpload.builder()
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
                .build();
        return SizeChart.builder()
                .image(image)
                .template(null)
                .build();
    }

    public List<Image> getMainImage (List<MainImage> src){
        return src.stream()
                .map(item -> Image.builder()
                        .uri(item.getUri())
                        .build()
                )
                .toList();
    }
}
