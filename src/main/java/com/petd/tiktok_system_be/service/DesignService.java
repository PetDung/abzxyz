package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.dto.request.DesignMappingRequest;
import com.petd.tiktok_system_be.dto.request.DesignRequest;
import com.petd.tiktok_system_be.entity.Design;
import com.petd.tiktok_system_be.entity.MappingDesign;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.DesignRepository;
import com.petd.tiktok_system_be.repository.MappingDesignRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DesignService {


    DesignRepository repository;
    MappingDesignRepository  mappingRepository;

    public Design getDesignById(String id) {
        return repository.findById(id).orElse(null);
    }

    public Design save(Design design){
        return repository.save(design);
    }
    public Design create(DesignRequest request) {
        Design design = Design.builder()
                .name(request.getName())
                .backSide(request.getBackSide())
                .frontSide(request.getFrontSide())
                .leftSide(request.getLeftSide())
                .rightSide(request.getRightSide())
                .build();
        return repository.save(design);
    }

    public Design getDesignBySkuIdAnhProductId (String skuId, String productId){
        MappingDesign mappingDesign = mappingRepository.findByProductIdAndSku(productId, skuId).orElse(null);
        if(mappingDesign == null){
            return null;
        }
        return mappingDesign.getDesign();
    }

    @Transactional
    public MappingDesign mappingDesignAndProduct (DesignMappingRequest request) {

        Design design = repository.findById(request.getDesignId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Optional<MappingDesign> existing = mappingRepository.findByProductIdAndDesign(request.getProductId(), design);

        for (String sku : request.getSkuIds()) {
            Optional<MappingDesign> conflict = mappingRepository.findByProductIdAndSku(request.getProductId(), sku);
            if (conflict.isPresent() && (existing.isEmpty() || !conflict.get().getId().equals(existing.get().getId()))) {
                // xoá sku khỏi record cũ
                MappingDesign oldMapping = conflict.get();
                oldMapping.getSkus().remove(sku);
                mappingRepository.save(oldMapping);
            }
        }

        MappingDesign mappingDesign = existing.orElseGet(() -> {
            MappingDesign md = new MappingDesign();
            md.setDesign(design);
            md.setProductId(request.getProductId());
            md.setSkus(new ArrayList<>());
            return md;
        });

        Set<String> updatedSkus = new HashSet<>(mappingDesign.getSkus());
        updatedSkus.addAll(request.getSkuIds());
        mappingDesign.setSkus(new ArrayList<>(updatedSkus));

        return mappingRepository.save(mappingDesign);
    }


}
