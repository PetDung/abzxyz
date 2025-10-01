package com.petd.tiktok_system_be.service.Shop;

import com.google.common.util.concurrent.RateLimiter;
import com.petd.tiktok_system_be.constant.Role;
import com.petd.tiktok_system_be.dto.request.DesignMappingRequest;
import com.petd.tiktok_system_be.dto.request.DesignRequest;
import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Design.Design;
import com.petd.tiktok_system_be.entity.Design.MappingDesign;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.DesignRepository;
import com.petd.tiktok_system_be.repository.MappingDesignRepository;
import com.petd.tiktok_system_be.repository.OrderItemRepository;
import com.petd.tiktok_system_be.service.Auth.AccountService;
import com.petd.tiktok_system_be.service.CloudinaryService;
import com.petd.tiktok_system_be.service.FileProxyService;
import com.petd.tiktok_system_be.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DesignService {


    DesignRepository repository;
    MappingDesignRepository  mappingRepository;
    OrderItemRepository orderItemRepository;
    AccountService accountService;
    FileProxyService fileProxyService;
    CloudinaryService cloudinaryService;
    NotificationService notificationService;

    public Design save(Design design){
        return repository.save(design);
    }
    public Design create(DesignRequest request) {

        Account account = accountService.getMe();
        String front = processLink(request.getFrontSide());
        String back = processLink(request.getBackSide());
        String left = processLink(request.getLeftSide());
        String right = processLink(request.getRightSide());

        String thumbnail = front;
        if (isNullOrEmpty(thumbnail)) thumbnail = back;
        if (isNullOrEmpty(thumbnail)) thumbnail = left;
        if (isNullOrEmpty(thumbnail)) thumbnail = right;

        Design design = Design.builder()
                .name(request.getName())
                .backSide(request.getBackSide())
                .frontSide(request.getFrontSide())
                .leftSide(request.getLeftSide())
                .rightSide(request.getRightSide())
                .front(front)
                .back(back)
                .leftUrl(left)
                .rightUrl(right)
                .thumbnail(thumbnail)
                .account(account)
                .build();
        return repository.save(design);
    }
    public void snyc () {
        List<Design> designs = repository.findAll();
        RateLimiter limiter = RateLimiter.create(5.0);

        designs.forEach(design -> {

            limiter.acquire(); // block tới khi đủ "token"

            String front = processLink(design.getFrontSide());
            String back = processLink(design.getBackSide());
            String left = processLink(design.getLeftSide());
            String right = processLink(design.getRightSide());

            String thumbnail = front;
            if (isNullOrEmpty(thumbnail)) thumbnail = back;
            if (isNullOrEmpty(thumbnail)) thumbnail = left;
            if (isNullOrEmpty(thumbnail)) thumbnail = right;

            design.setFront(front);
            design.setBack(back);
            design.setLeftUrl(left);
            design.setRightUrl(right);
            design.setThumbnail(thumbnail);
        });
        repository.saveAll(designs);
    }
    public void aDesnyc (String id) {
        Design design = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        String front = processLink(design.getFrontSide());
        String back = processLink(design.getBackSide());
        String left = processLink(design.getLeftSide());
        String right = processLink(design.getRightSide());

        String thumbnail = front;
        if (isNullOrEmpty(thumbnail)) thumbnail = back;
        if (isNullOrEmpty(thumbnail)) thumbnail = left;
        if (isNullOrEmpty(thumbnail)) thumbnail = right;

        design.setFront(front);
        design.setBack(back);
        design.setLeftUrl(left);
        design.setRightUrl(right);
        design.setThumbnail(thumbnail);

        repository.save(design);
    }
    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public List<Design> getAllDesigns() {
        Account account = accountService.getMe();
        if(account.getRole().equals(Role.Admin.toString())){
            return repository.findAll();
        }
        String accountId = null;
        if(account.getRole().equals(Role.Leader.toString())) accountId = account.getId();
        if (account.getRole().equals(Role.Employee.toString())) accountId = account.getTeam().getLeader().getId();
        return repository.findAllByAccount_Id(accountId);
    }

    public void deleteDesignById(String id) {
        Design design = repository.findById(id).orElse(null);
        if (design == null) return;
        repository.deleteById(id);
        cloudinaryService.deleteByUrl(design.getThumbnail());
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
                notificationService.orderUpdateStatus(orderItem.getOrder());
            });
            orderItemRepository.saveAll(orderItems);
        }
        return updatedMappingDesign;
    }


    public void clearDesignInOrderItem (List<String> itemIds) {
        List<OrderItem> items = orderItemRepository.findAllById(itemIds);
          items.forEach(orderItem -> {
                orderItem.setDesign(null);
        });
        orderItemRepository.saveAll(items);
        notificationService.orderUpdateStatus(items.get(0).getOrder());
    }
    @Transactional
    public void removeSkus(String productId, List<String> skusToRemove) {
        List<MappingDesign> mappings = mappingRepository.findByProductId(productId);

        for (MappingDesign md : mappings) {
            List<String> skus = md.getSkus();
            boolean changed = skus.removeAll(skusToRemove);
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

    private String processLink(String url) {
        if (url == null || url.isBlank()) return null;

        if (isGoogleDriveLink(url)) {
            try {
                // 1. Lấy fileId từ URL
                String fileId = extractFileId(url);

                // 2. Download file từ Google Drive
                MultipartFile file = fileProxyService.downloadFileAsMultipart(fileId, "original");

                // 3. Upload lên Cloud (ví dụ Cloudinary)
                String cloudUrl = cloudinaryService.uploadFile(file); // giả sử bạn có service upload

                return cloudUrl;

            } catch (Exception e) {
                log.error("Lỗi xử lý link Google Drive: {}", url, e);
                return url; // fallback: trả link gốc
            }
        }
        return url; // không phải Drive -> giữ nguyên
    }

    private boolean isGoogleDriveLink(String url) {
        return url.contains("drive.google.com");
    }

    private String extractFileId(String url) {
        // Ví dụ: https://drive.google.com/file/d/<fileId>/view?usp=sharing
        String[] parts = url.split("/d/");
        if (parts.length < 2) return null;
        String idPart = parts[1];
        return idPart.split("/")[0];
    }


}
