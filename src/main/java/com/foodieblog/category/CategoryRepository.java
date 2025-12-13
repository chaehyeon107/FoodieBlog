package com.foodieblog.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);

    // 수정 시 자기 자신 제외 중복체크용
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<Category> findBySlug(String slug);
}
