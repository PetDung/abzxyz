package com.petd.tiktok_system_be.Specification;

import com.petd.tiktok_system_be.entity.Return.Return;
import org.springframework.data.jpa.domain.Specification;

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
}
