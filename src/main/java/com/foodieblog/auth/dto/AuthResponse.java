package com.foodieblog.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "인증 응답 DTO (로그인 / 토큰 재발급 공통)")
public class AuthResponse {

    @Schema(
            description = "JWT Access Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(
            description = "JWT Refresh Token (로그인 시에만 포함)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "로그인 사용자 정보 (토큰 재발급 시에는 null)")
    private UserDto user;

    @Getter
    @AllArgsConstructor
    @Schema(description = "로그인 사용자 정보")
    public static class UserDto {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이메일", example = "user1@example.com")
        private String email;

        @Schema(description = "닉네임", example = "채현")
        private String nickname;

        @Schema(description = "권한", example = "ROLE_USER")
        private String role;
    }

    /** 로그인 응답용 */
    public static AuthResponse forLogin(String accessToken, String refreshToken, UserDto user) {
        return new AuthResponse(accessToken, refreshToken, user);
    }

    /** 토큰 재발급 응답용 */
    public static AuthResponse forRefresh(String accessToken) {
        return new AuthResponse(accessToken, null, null);
    }
}
