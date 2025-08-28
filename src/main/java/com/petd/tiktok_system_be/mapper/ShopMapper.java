package com.petd.tiktok_system_be.mapper;

import com.petd.tiktok_system_be.dto.response.ShopResponse;
import com.petd.tiktok_system_be.entity.Shop;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShopMapper {

    ShopResponse toShopResponse(Shop shop);
    List<ShopResponse> toShopResponseList(List<Shop> shops);
}
