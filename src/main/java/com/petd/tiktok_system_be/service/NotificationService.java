package com.petd.tiktok_system_be.service;

import com.petd.tiktok_system_be.entity.Auth.Account;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.service.Auth.AccountService;
import com.petd.tiktok_system_be.service.Order.OrderService;
import com.petd.tiktok_system_be.service.Shop.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {


    SimpMessagingTemplate messagingTemplate;
    AccountService accountService;

    public void orderUpdateStatus(Order order){
        final OrderMessage<Order> orderMessage = "ON_HOLD".equals(order.getStatus())
                ? new OrderMessage<Order>(order, "NEW_ORDER")
                : new OrderMessage<Order>(order, "UPDATE");

        List<Account> accounts = accountService.getAllAccountsAccessShop(order.getShop());
        accounts.forEach(account -> {
            messagingTemplate.convertAndSendToUser(
                    account.getUsername(),
                    "/queue/orders",
                    orderMessage
            );
        });
    }
    public record OrderMessage<T>(
            T data,
            String event
    ){}
}
