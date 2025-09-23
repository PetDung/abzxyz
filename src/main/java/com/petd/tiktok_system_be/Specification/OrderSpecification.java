package com.petd.tiktok_system_be.Specification;


import com.petd.tiktok_system_be.entity.Order.Order;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


public class OrderSpecification {

    public static Specification<Order> filterOrders(
            String orderId,
            List<String> shopIds,
            List<String> statuses,
            String shippingType
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Nếu có orderId thì chỉ lọc theo id
            if (orderId != null && !orderId.isEmpty()) {
                predicates.add(cb.equal(root.get("id"), orderId));
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // Nếu có shopIds
            if (shopIds != null && !shopIds.isEmpty()) {
                predicates.add(root.get("shop").get("id").in(shopIds));
            }

            // Nếu có statuses
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }


            // Nếu có shippingType
            if (shippingType != null && !shippingType.isEmpty()) {
                predicates.add(cb.equal(root.get("shippingType"), shippingType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
