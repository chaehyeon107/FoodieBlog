package com.foodieblog.user.dto;

import com.foodieblog.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserResponse from(User u) {
        return new UserResponse(
                u.getUserId(),
                u.getEmail(),
                u.getNickname(),
                u.getRole().name(),
                u.isActive(),
                u.getCreatedAt(),
                u.getLastLoginAt()
        );
    }
}
