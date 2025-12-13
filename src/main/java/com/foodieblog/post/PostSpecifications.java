package com.foodieblog.post;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class PostSpecifications {

    private PostSpecifications() {}

    public static Specification<Post> keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String k = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), k),
                cb.like(cb.lower(root.get("content")), k),
                cb.like(cb.lower(root.get("restaurantName")), k)
        );
    }

    public static Specification<Post> categoryId(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Post> status(PostStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    // 방문일(visitedAt) 기준 필터로 구현 (요구사항 dateFrom/dateTo)
    public static Specification<Post> visitedFrom(LocalDate from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("visitedAt"), from);
    }

    public static Specification<Post> visitedTo(LocalDate to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("visitedAt"), to);
    }
}
