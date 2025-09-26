package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.api.UploadImageApi;
import com.petd.tiktok_system_be.api.UploadProductApi;
import com.petd.tiktok_system_be.api.body.productRequestUpload.*;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Product.UploadedProduct;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.UploadProductRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import com.petd.tiktok_system_be.service.TelegramService;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UploadProductCase {

    RequestClient requestClient;
    ShopService shopService;
    TelegramService telegramService;
    UploadProductRepository uploadProductRepository;


    public JsonNode uploadProductCase(ProductUpload product, String shopId) throws IOException {
       try{
           telegramService.sendMessage("Upload " + product.getProductOriginId());
           Shop shop = shopService.getShopByShopId(shopId);
           if(StringUtils.isBlank(shop.getWarehouse())){
               throw new AppException(ErrorCode.RQ);
           }
           handleImageUpload(product, shop);
           UploadProductApi uploadProductApi = UploadProductApi.builder()
                   .shopCipher(shop.getCipher())
                   .accessToken(shop.getAccessToken())
                   .body(product)
                   .requestClient(requestClient)
                   .build();
           TiktokApiResponse tiktokApiResponse = uploadProductApi.callApi();

           if(!uploadProductRepository.existsByProductIdAndShop(product.getProductOriginId(), shop)){
               UploadedProduct uploadedProduct = UploadedProduct.builder()
                       .productId(product.getProductOriginId())
                       .shop(shop)
                       .build();
               uploadProductRepository.save(uploadedProduct);
           }

           telegramService.sendMessage("Upload product successfully " + shop.getUserShopName());
           return tiktokApiResponse.getData();
       }catch(TiktokException ex){
           telegramService.sendMessage("Upload product failed " + ex.getMessage());
           ex.printStackTrace();
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }catch(Exception ex){
           telegramService.sendMessage("Upload product failed by system " + ex.getMessage());
           ex.printStackTrace();
           throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
       }
    }

    private void handleImageUpload(ProductUpload productUpload, Shop shop) throws IOException {
        // Upload main images
        productUpload.setMainImages(uploadImages(productUpload.getMainImages(), "MAIN_IMAGE", shop));

        // Upload SKU attribute images
        List<SkuUpload> skuUploads = productUpload.getSkus().stream().map(sku -> {
            List<SalesAttribute> updatedAttributes = sku.getSalesAttributes().stream().map(attr -> {
                if (attr.getSkuImg() != null && !attr.getSkuImg().getUrls().isEmpty()) {
                    try {
                        Image uploaded = uploadSingleImage(attr.getSkuImg().getUrls().get(0), "ATTRIBUTE_IMAGE", shop);
                        attr.setSkuImg(uploaded);
                    } catch (IOException e) {
                        log.error("Upload attribute image failed", e);
                        throw new RuntimeException(e);
                    }
                }
                return SalesAttribute.builder()
                        .skuImg(attr.getSkuImg())
                        .name(attr.getName())
                        .valueName(attr.getValueName())
                        .build();
            }).toList();

            List<Inventory> inventories = sku.getInventory().stream()
                    .map(i -> Inventory.builder()
                            .warehouseId(shop.getWarehouse())
                            .quantity(1000)
                            .build())
                    .toList();

            Price price = Price.builder()
                    .salePrice(sku.getPrice().getSalePrice())
                    .amount(sku.getPrice().getSalePrice())
                    .currency(sku.getPrice().getCurrency())
                    .build();

            return SkuUpload.builder()
                    .combinedSkus(sku.getCombinedSkus())
                    .inventory(inventories)
                    .salesAttributes(updatedAttributes)
                    .externalSkuId(sku.getExternalSkuId())
                    .price(price)
                    .sellerSku(sku.getSellerSku())
                    .preSale(sku.getPreSale())
                    .externalUrls(sku.getExternalUrls())
                    .identifierCode(sku.getIdentifierCode())
                    .extraIdentifierCodes(sku.getExtraIdentifierCodes())
                    .listPrice(sku.getListPrice())
                    .externalListPrices(sku.getExternalListPrices())
                    .build();
        }).toList();
        productUpload.setSkus(skuUploads);

        // Upload size chart image
        if (productUpload.getSizeChart() != null && productUpload.getSizeChart().getImage() != null) {
            Image uploaded = uploadSingleImage(productUpload.getSizeChart().getImage().getUrls().get(0), "SIZE_CHART_IMAGE", shop);
            productUpload.setSizeChart(SizeChart.builder().image(uploaded).build());
        }
    }

    private List<Image> uploadImages(List<Image> images, String useCase, Shop shop) {
        List<Image> uploadedImages = new ArrayList<>();
        for (Image img : images) {
            if (img.getUrls() != null && !img.getUrls().isEmpty()) {
                try {
                    uploadedImages.add(uploadSingleImage(img.getUrls().get(0), useCase, shop));
                } catch (IOException e) {
                    log.error("Upload image failed: {}", img.getUrls().get(0), e);
                    throw new RuntimeException(e);
                }
            }
        }
        return uploadedImages;
    }

    private Image uploadSingleImage(String url, String useCase, Shop shop) throws IOException {
        File tempFile = downloadImage(url);
        try {
            UploadImageApi uploadImageApi = UploadImageApi.builder()
                    .useCase(useCase)
                    .accessToken(shop.getAccessToken())
                    .requestClient(requestClient)
                    .image(tempFile)
                    .build();
            TiktokApiResponse response = uploadImageApi.callApi();
            String uploadedUri = response.getData().get("uri").asText();
            return Image.builder().uri(uploadedUri).build();
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    private File downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        File tempFile = File.createTempFile("image_", ".jpg");
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
