package com.petd.tiktok_system_be.service.Shop;

import com.petd.tiktok_system_be.dto.request.DesignMappingRequest;
import com.petd.tiktok_system_be.dto.request.DesignRequest;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Design.MappingDesign;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.DesignRepository;
import com.petd.tiktok_system_be.repository.MappingDesignRepository;
import com.petd.tiktok_system_be.repository.OrderItemRepository;
import com.petd.tiktok_system_be.service.Order.OrderService;
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
    OrderItemRepository orderItemRepository;

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
    public List<Design> getAllDesigns() {
        return repository.findAll();
    }

    public void deleteDesignById(String id) {
        repository.deleteById(id);
    }

    public Design getDesignBySkuIdAnhProductId (String skuId, String productId){
        MappingDesign mappingDesign = mappingRepository.findByProductIdAndSku(productId, skuId).orElse(null);
        if(mappingDesign == null){
            return null;
        }
        return mappingDesign.getDesign();
    }


    public Map<String, Design> getDesignBySkusIdAndProductId(String[] skuIds, String productId) {
        List<Object[]> rows = mappingRepository.findDesignsByProductIdAndSkuIds(productId, skuIds);
        Map<String,Design> map = new HashMap<>();

        for(Object[] row : rows){
            String skuId = (String)row[1];
            String designId = (String)row[0];
            Design design  = repository.findById(designId).orElse(null);
            map.put(skuId,design);
        }
        return map;
    }

    @Transactional
    public MappingDesign mappingDesignAndProduct (DesignMappingRequest request) {

        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }
        if (request.getDesignId() == null || request.getDesignId().trim().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }

        if (request.getSkuIds() == null || request.getSkuIds().isEmpty()) {
            throw new AppException(ErrorCode.RQ);
        }


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

        MappingDesign updatedMappingDesign = mappingRepository.save(mappingDesign);

        for (String sku : updatedSkus ) {
            List<OrderItem> orderItems = orderItemRepository.findBySkuIdAndProductId(sku,
                    updatedMappingDesign.getProductId());
            orderItems.forEach(orderItem -> {
                orderItem.setDesign(mappingDesign.getDesign());
            });
            orderItemRepository.saveAll(orderItems);
        }
        return updatedMappingDesign;
    }

    @Transactional
    public void removeSkus(String productId, List<String> skusToRemove) {
        List<MappingDesign> mappings = mappingRepository.findByProductId(productId);

        for (MappingDesign md : mappings) {
            List<String> skus = md.getSkus();
            boolean changed = skus.removeAll(skusToRemove); // remove các sku cần xoá
            for (String sku : skus ) {
                List<OrderItem> orderItems = orderItemRepository.findBySkuIdAndProductId(sku,
                        md.getProductId());
                orderItems.forEach(orderItem -> {
                    orderItem.setDesign(null);
                });
                orderItemRepository.saveAll(orderItems);
            }
            if (changed) {

                if (skus.isEmpty()) {
                    mappingRepository.delete(md); // nếu không còn SKU thì delete record
                } else {
                    md.setSkus(skus); // update jsonb
                    mappingRepository.save(md);
                }
            }
        }
    }


}
