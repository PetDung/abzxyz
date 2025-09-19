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
    OrderRepository orderRepository;

    public void orderUpdateStatus(Order order){
        List<Account> accounts = accountService.getAllAccountsAccessShop(order.getShop());
        accounts.forEach(account -> {
            messagingTemplate.convertAndSendToUser(
                    account.getUsername(),
                    "/queue/orders",
                    order
            );
        });
    }

    public void orderUpdateStatus(String orderId){
        Order order = orderRepository.findById(orderId).get();
        List<Account> accounts = accountService.getAllAccountsAccessShop(order
                .getShop());
        accounts.forEach(account -> {
            messagingTemplate.convertAndSendToUser(
                    account.getUsername(),
                    "/queue/orders",
                    order
            );
        });
    }


    public void orderUpdateStatus(List<Order> orders){
        List<Account> accounts = accountService.getAllAccountsAccessShop(orders.get(0).getShop());
        accounts.forEach(account -> {
            messagingTemplate.convertAndSendToUser(
                    account.getUsername(),
                    "/queue/orders",
                    orders
            );
        });
    }
}
