package com.petd.tiktok_system_be.service.Notification;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Product.Product;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationOrderService {

    TelegramService telegramService;

    public void orderNotification(Order order){

        String shopName = "%s(%s)".formatted(product.getShop().getUserShopName(), product.getShop().getTiktokShopName());
        String productId = product.getId();
        String reason = "";
        String productName = product.getTitle();
        if(!product.getAuditFailedReasons().isEmpty()){
            reason = product.getAuditFailedReasons().get(0).getReasons().toString();
        }
        String imageUrl = product.getMainImages().get(0).getUrls().get(0);

        String caption = buildProductFreezeMessage(shopName, productId, productName, reason);
        String chatId = "-1003152164716";
        telegramService.sendPhoto(imageUrl, caption, chatId);
    }

    public String buildOrderMessage(String shortName,
                                            String shopName,
                                            String sellerName,
                                            String amount,
                                            String length,
                                            String itemName,
                                            String id,
                                            String createdAt

                                            ) {
        return """
            🚀 New order: %s
            ✅ Shop: %s
            🤝 Seller: %s
            💰 Est. Amount: %s USD
            📦 Items: %s
            🧾 Name: %s
            📄 Order ID: %s
            📅 Date: %s
            """.formatted(shortName, shopName, sellerName, amount, length, itemName, id, createdAt);
    }
}
