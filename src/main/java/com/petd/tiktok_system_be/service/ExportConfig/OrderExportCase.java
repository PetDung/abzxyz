package com.petd.tiktok_system_be.service.ExportConfig;

import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.Setting;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.service.GoogleSevice.GoogleSheetService;
import com.petd.tiktok_system_be.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderExportCase implements ExportInterface {


    SettingRepository settingRepository;
    OrderRepository orderRepository;
    GoogleSheetService<OrderExport> googleSheetService;
    Export export;
    NotificationService notificationService;

    @Transactional
    @Override
    public Map<String, String> run(List<String> items) {
        Map<String, String> errors = new HashMap<>();
        Setting setting = settingRepository.findAll().get(0);

        for (String orderId : items) {
            try {
                List<OrderExport> orderExports = export.run(orderId);
                googleSheetService.exportOrdersToSheet(
                        setting.getOrderSheetId(),
                        setting.getOrderAllSheetName(),
                        orderExports,
                        OrderExport.class,
                        "key"
                );
                Order order = orderRepository.findById(orderId).get();
                order.setIsNote(true);
                orderRepository.save(order);
                notificationService.orderUpdateStatus(order);
            } catch (Exception e) {
                errors.put(orderId, e.getMessage());
            }
        }
        return errors; // Map rỗng nếu không có lỗi
    }
}
