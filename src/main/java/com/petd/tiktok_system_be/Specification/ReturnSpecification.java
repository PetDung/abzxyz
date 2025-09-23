package com.petd.tiktok_system_be.Specification;

import com.petd.tiktok_system_be.entity.Manager.Shop;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.entity.Return.Return;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ReturnSpecification {

    public static Specification<Return> hasOrderId(String orderId) {
        return (root, query, cb) -> {
            if (orderId == null || orderId.isEmpty()) {
                return cb.conjunction(); // không filter
            }
            return cb.equal(root.join("order").get("id"), orderId);
        };
    }

    public static Specification<Return> hasReturnId(String returnId) {
        return (root, query, cb) -> {
            if (returnId == null || returnId.isEmpty()) {
                return cb.conjunction(); // không filter
            }
            return cb.equal(root.get("returnId"), returnId);
        };
    }

    public static Specification<Return> hasShopIds(List<String> shopIds) {
        return (root, query, cb) -> {
            if (shopIds == null || shopIds.isEmpty()) {
                return cb.conjunction(); // không filter
            }
            Join<Return, Order> orderJoin = root.join("order");
            Join<Order, Shop> shopJoin = orderJoin.join("shop");
            return shopJoin.get("id").in(shopIds);
        };
    }
}
