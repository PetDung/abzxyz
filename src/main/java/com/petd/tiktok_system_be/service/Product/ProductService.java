package com.petd.tiktok_system_be.service.Product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.Specification.ProductSpecification;
import com.petd.tiktok_system_be.api.GetProduct;
import com.petd.tiktok_system_be.dto.response.ProductResponse;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.repository.ProductRepository;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {

    RequestClient requestClient;
    ShopService shopService;
    ProductRepository productRepository;

    public JsonNode getProduct (String shopId, String productId){
        Shop shop = shopService.getShopByShopId(shopId);

        GetProduct getProduct = GetProduct.builder()
                .requestClient(requestClient)
                .productId(productId)
                .shopCipher(shop.getCipher())
                .accessToken(shop.getAccessToken())
                .build();
        try {
            TiktokApiResponse response = getProduct.callApi();
            return response.getData();
        }catch (TiktokException e) {
            log.error(e.getMessage());
            throw new AppException(e.getMessage(), e.getCode());
        } catch (JsonProcessingException e) {
            throw new AppException(e.getMessage(), 409);
        }
    }

    public List<Product> getAllActiveProducts(Map<String, String> param) {
        List<String> allowedFilters = Arrays.asList("ACTIVE", "UPDATE");

        // Lấy tất cả shop của user
        List<Shop> myShops = shopService.getMyShops();
        List<String> shopIds = myShops.stream()
                .map(Shop::getId)
                .toList();

        Long endDate = parseNumberSafe(param.get("end_time"),Long::parseLong, null);
        Long startDate = parseNumberSafe(param.get("start_time"),Long::parseLong,null);

        String filterParam = param.get("filter");
        String filter = (filterParam != null && allowedFilters.contains(filterParam))
                ? filterParam
                : "ACTIVE";



        // Tạo Specification: chỉ lấy ACTIVE và thuộc shopIds
        Specification<Product> spec = Specification
                .where(ProductSpecification.hasStatus(List.of("ACTIVATE")))
                .and(ProductSpecification.hasShopIds(shopIds));

        if(filter.equals("ACTIVE")) {
            spec = spec.and(ProductSpecification.activeTimeBetween(startDate, endDate));
        }else if(filter.equals("UPDATE")) {
            spec = spec.and(ProductSpecification.updateTimeBetween(startDate, endDate));
        }

        // Sắp xếp theo activeTime giảm dần
        Sort sort = Sort.by(Sort.Order.desc("activeTime"));

        // Không phân trang, lấy tất cả
        return productRepository.findAll(spec,sort);
    }

    public ProductResponse getListProductInDataBase(Map<String, String> param) {

        List<String> allowedFilters = Arrays.asList("ACTIVE", "UPDATE");

        int page = parseNumberSafe(param.get("page"), Integer::parseInt, 0);
        int pageSize = parseNumberSafe(param.get("pageSize"), Integer::parseInt, 10);
        List<String> statuses = param.containsKey("status")
                ? Arrays.asList(param.get("status").split(","))
                : List.of("ACTIVATE");
        String keyword = param.get("keyword");
        Long endDate = parseNumberSafe(param.get("end_time"),Long::parseLong, null);
        Long startDate = parseNumberSafe(param.get("start_time"),Long::parseLong,null);

        String filterParam = param.get("filter");
        String filter = (filterParam != null && allowedFilters.contains(filterParam))
                ? filterParam
                : "ACTIVE";



        // Lấy tất cả shop của user
        List<Shop> myShops = shopService.getMyShops();
        List<String> shopIds = myShops.stream()
                .map(Shop::getId)
                .toList();

        // Tạo Specification
        Specification<Product> spec = Specification
                .where(ProductSpecification.hasStatus(statuses))
                .and(ProductSpecification.hasShopIds(shopIds))
                .and(ProductSpecification.hasIdOrTitleLike(keyword));

        String sortBy = "activeTime";

        if(filter.equals("ACTIVE")) {
            spec = spec.and(ProductSpecification.activeTimeBetween(startDate, endDate));
        }else if(filter.equals("UPDATE")) {
            spec = spec.and(ProductSpecification.updateTimeBetween(startDate, endDate));
            sortBy = "updateTime";
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(
                Sort.Order.desc(sortBy),
                Sort.Order.desc("id")
        ));
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return ProductResponse.builder()
                .products(productPage.getContent())
                .currentPage(page)
                .isLast(productPage.isLast())
                .totalCount(productPage.getTotalElements())
                .build();
    }

    private <T extends Number> T parseNumberSafe(
            String value,
            Function<String, T> parser,
            T defaultValue
    ) {
        try {
            return (value != null && !value.isBlank()) ? parser.apply(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
