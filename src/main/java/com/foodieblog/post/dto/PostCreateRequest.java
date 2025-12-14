package com.foodieblog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "식당 이름은 필수입니다.")
    private String restaurantName;

    private String address;

    @PastOrPresent(message = "방문일은 오늘 또는 과거 날짜여야 합니다.")
    private LocalDate visitedAt;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;
}
