package com.foodieblog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    @Size(max = 100)
    private String restaurantName;

    private String address;

    private LocalDate visitedAt;

    @NotNull
    private Long categoryId;
}
