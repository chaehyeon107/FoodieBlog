package com.foodieblog.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String role;
}
