package com.petd.tiktok_system_be.service.ExportConfig;

import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.service.Auth.SettingService;
import com.petd.tiktok_system_be.service.GoogleSevice.GoogleSheetService;
import com.petd.tiktok_system_be.service.Lib.NotificationService;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    TelegramService telegramService;
    SettingService settingService;

    @Transactional
    @Override
    public Map<String, String> run(List<String> items) {
        Map<String, String> errors = new HashMap<>();
        Setting setting = settingService.getSetting();

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
                String exceptionClass = e.getClass().getName();

                // Lấy message của exception
                String exceptionMessage = e.getMessage();

                // Lấy toàn bộ stack trace
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();

                String fullError = String.format(
                        "Exception Class: %s\nMessage: %s\nStack Trace:\n%s",
                        exceptionClass,
                        exceptionMessage,
                        stackTrace
                );
                telegramService.sendMessage(fullError);
                errors.put(orderId, fullError);
            }
        }
        return errors; // Map rỗng nếu không có lỗi
    }
}
