package com.petd.tiktok_system_be.mapper;

import com.petd.tiktok_system_be.dto.response.GroupResponse;
import com.petd.tiktok_system_be.entity.ShopGroup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupResponse toGroupResponse(ShopGroup group);

    List<GroupResponse> toGroupResponseList(List<ShopGroup> groups);
}
