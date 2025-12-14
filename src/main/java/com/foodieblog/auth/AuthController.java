package com.foodieblog.auth;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.auth.dto.AuthResponse;
import com.foodieblog.auth.dto.LoginRequest;
import com.foodieblog.auth.dto.MeResponse;
import com.foodieblog.auth.dto.RefreshRequest;
import com.foodieblog.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ✅ 공통 ErrorResponse schema (반복 줄이기용으로 import alias가 안 돼서 그냥 schema만 통일)
    private static final Class<?> ERROR_SCHEMA = com.foodieblog.common.error.ErrorResponse.class;

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인하여 Access/Refresh Token을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 (VALIDATION_FAILED, BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "요청 한도 초과 (TOO_MANY_REQUESTS)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "요청 본문이 너무 큼 (PAYLOAD_TOO_LARGE)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        System.out.println("[LOGIN HIT] email=" + req.getEmail());
        return ApiResponse.ok(authService.login(req));
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 기본 정보를 반환합니다. (JWT 필요)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음 또는 비활성 계정 (FORBIDDEN, USER_DEACTIVATED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(new MeResponse(
                principal.userId(),
                principal.email(),
                principal.nickname(),
                principal.role()
        ));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 Access Token(및 필요 시 Refresh Token)을 재발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 (VALIDATION_FAILED, BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token이 없거나 유효하지 않음/만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "처리 불가 (UNPROCESSABLE_ENTITY) - 예: Refresh Token 형식은 맞지만 논리적으로 사용 불가",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "요청 한도 초과 (TOO_MANY_REQUESTS)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "요청 본문이 너무 큼 (PAYLOAD_TOO_LARGE)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.getRefreshToken()));
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하여 로그아웃 처리합니다. (서버가 Refresh Token을 저장/관리하는 경우에 유효)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 (VALIDATION_FAILED, BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "처리 불가 (UNPROCESSABLE_ENTITY) - 예: Refresh Token이 이미 무효화됨",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "요청 한도 초과 (TOO_MANY_REQUESTS)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "요청 본문이 너무 큼 (PAYLOAD_TOO_LARGE)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody com.foodieblog.auth.dto.LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return ApiResponse.ok(null);
    }
}
