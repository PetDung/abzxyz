package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.request.DesignMappingRequest;
import com.petd.tiktok_system_be.dto.request.DesignRequest;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.entity.Design;
import com.petd.tiktok_system_be.entity.MappingDesign;
import com.petd.tiktok_system_be.service.DesignService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/design")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DesignController {

    DesignService designService;

    @PostMapping
    public ApiResponse<Design> createDesign(@RequestBody DesignRequest request) {
        return ApiResponse.<Design>builder()
                .result(designService.create(request))
                .build();
    }

    @PostMapping("/mapping-design")
    public ApiResponse<MappingDesign> createDesign(@RequestBody DesignMappingRequest request) {
        return ApiResponse.<MappingDesign>builder()
                .result(designService.mappingDesignAndProduct(request))
                .build();
    }

}
