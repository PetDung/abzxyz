package com.petd.tiktok_system_be.Specification;

import com.petd.tiktok_system_be.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecification {

    public static Specification<Product> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
    public static Specification<Product> hasStatus(List<String> statuses) {
        return (root, query, cb) ->
                (statuses == null || statuses.isEmpty())
                        ? cb.conjunction()
                        : root.get("status").in(statuses);
    }
    public static Specification<Product> hasTitleLike(String keyword) {
        return (root, query, cb) ->
                keyword == null ? cb.conjunction() : cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Product> hasIdOrTitleLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // không filter gì
            }

            // Predicate cho title (LIKE, không phân biệt hoa thường)
            Predicate titlePredicate = cb.like(
                    cb.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            );

            // Predicate cho id (so sánh chính xác, nếu keyword là số)
            Predicate idPredicate = cb.disjunction();
            try {
                Long idValue = Long.parseLong(keyword);
                idPredicate = cb.equal(root.get("id"), idValue);
            } catch (NumberFormatException ignored) {
                // Nếu keyword không phải số -> bỏ qua tìm theo id
            }

            // Kết hợp OR giữa id và title
            return cb.or(titlePredicate, idPredicate);
        };
    }

    public static Specification<Product> hasShopId(String shopId) {
        return (root, query, cb) ->
                shopId == null ? cb.conjunction() : cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Product> hasShopIds(List<String> shopIds) {
        return (root, query, cb) -> {
            if (shopIds == null || shopIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("shop").get("id").in(shopIds);
        };
    }

    public static Specification<Product> activeTimeBetween(Long from, Long to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("activeTime"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("activeTime"), from);
            }
            return cb.lessThanOrEqualTo(root.get("activeTime"), to);
        };
    }

    public static Specification<Product> updateTimeBetween(Long from, Long to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("updateTime"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("updateTime"), from);
            }
            return cb.lessThanOrEqualTo(root.get("updateTime"), to);
        };
    }

}
