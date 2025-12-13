package com.foodieblog.category.dto;

import com.foodieblog.category.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private String name;
    private String slug;

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug());
    }
}
