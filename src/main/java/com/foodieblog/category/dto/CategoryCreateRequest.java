package com.foodieblog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank(message = "name은 필수입니다.")
    @Size(max = 50, message = "name은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "slug는 필수입니다.")
    @Size(max = 80, message = "slug는 80자 이하여야 합니다.")
    private String slug;
}
