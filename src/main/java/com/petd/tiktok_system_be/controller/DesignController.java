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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ApiResponse<List<Design>> getDesigns() {
        return ApiResponse.<List<Design>>builder()
                .result(designService.getAllDesigns())
                .build();
    }


    @PostMapping("/mapping-design")
    public ApiResponse<MappingDesign> createDesignMapping(@RequestBody DesignMappingRequest request) {
        return ApiResponse.<MappingDesign>builder()
                .result(designService.mappingDesignAndProduct(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteDesignById(@PathVariable String id) {
        designService.deleteDesignById(id);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

}
