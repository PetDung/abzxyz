package com.petd.tiktok_system_be.service.Notification;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Order.OrderItem;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import com.petd.tiktok_system_be.util.DateConvert;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationOrderService {

    TelegramService telegramService;

    public void orderNotification(Order order){

        String shopName = "%s(%s)".formatted(order.getShop().getUserShopName(), order.getShop().getTiktokShopName());
        String sellerName = "Thông";
        String amount = order.getPaymentAmount().toString()  ;
        String length = String.valueOf(order.getLineItems().size());
        List<String> nameList = order.getLineItems().stream()
                .map(OrderItem::getProductName)
                .toList();
        String itemName = nameList.toString();
        String id = order.getId();
        String createdAt = DateConvert.toLocalDateTime(order.getCreateTime());

        String caption = buildOrderMessage(shopName, shopName, sellerName, amount,  length , itemName , id, createdAt);
        String chatId = "-1003152164716";
        telegramService.sendMessage(caption, chatId);
    }

    public String buildOrderMessage(String shortName,
                                    String shopName,
                                    String sellerName,
                                    String amount,
                                    String length,
                                    String itemName,
                                    String id,
                                    String createdAt) {
        return """
        🚀 <b>New Order:</b> %s

        ✅ <b>Shop:</b> %s
        🤝 <b>Seller:</b> %s
        💰 <b>Est. Amount:</b> <code>%s USD</code>
        📦 <b>Items:</b> %s
        🧾 <b>Name:</b> %s
        📄 <b>Order ID:</b> <code>%s</code>
        📅 <b>Date:</b> %s
        """.formatted(shortName, shopName, sellerName, amount, length, itemName, id, createdAt);
    }

}
