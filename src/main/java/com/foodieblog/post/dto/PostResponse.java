package com.foodieblog.post.dto;

import com.foodieblog.post.Post;
import com.foodieblog.post.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String restaurantName;
    private String address;
    private LocalDate visitedAt;

    private PostStatus status;

    private Long authorId;

    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .restaurantName(post.getRestaurantName())
                .address(post.getAddress())
                .visitedAt(post.getVisitedAt())
                .status(post.getStatus())
                .authorId(post.getAuthorId())
                .categoryId(post.getCategory().getId())
                .categoryName(post.getCategory().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
