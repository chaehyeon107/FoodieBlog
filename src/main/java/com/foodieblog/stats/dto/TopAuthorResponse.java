package com.foodieblog.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopAuthorResponse {
    private Long authorId;
    private String nickname;
    private long postCount;
}
