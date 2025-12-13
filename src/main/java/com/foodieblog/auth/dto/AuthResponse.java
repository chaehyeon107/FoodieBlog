package com.foodieblog.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String refreshToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDto user;

    @Getter
    @AllArgsConstructor
    public static class UserDto {
        private Long userId;
        private String email;
        private String nickname;
        private String role;
    }

    public static AuthResponse forLogin(String accessToken, String refreshToken, UserDto user) {
        return new AuthResponse(accessToken, refreshToken, user);
    }

    public static AuthResponse forRefresh(String accessToken) {
        return new AuthResponse(accessToken, null, null);
    }
}
