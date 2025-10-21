package com.petd.tiktok_system_be.service.Notification;
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
public class NotificationProductService {

    TelegramService telegramService;

    public void productNotification(Product product){

        String shopName = "%s(%s)".formatted(product.getShop().getUserShopName(), product.getShop().getTiktokShopName());
        String productId = product.getId();
        String reason = "";
        String suggestions = "";
        String productName = product.getTitle();
        if(!product.getAuditFailedReasons().isEmpty()){
            reason = product.getAuditFailedReasons().get(0).getReasons().toString();
            suggestions = product.getAuditFailedReasons().get(0).getSuggestions().toString();
        }
        String imageUrl = product.getMainImages().get(0).getUrls().get(0);

        String caption = buildProductFreezeMessage(shopName, productId, productName, reason, suggestions);
        String chatId = "-1003152164716";
        telegramService.sendPhoto(imageUrl, caption, chatId);
    }

    public String buildProductFreezeMessage(String shopName, String productId, String productName, String reason, String suggestions) {
        return """
            🔴 <b>#PRODUCT_FREEZE</b> 🔴
            - Shop: <b>%s</b>
            - Product ID: <code>%s</code>
            - Product Name: <i>%s</i>
            
            - Reason: <b>%s</b>
            - Suggestions: %s
            """.formatted(shopName, productId, productName, reason, suggestions);
    }
}
