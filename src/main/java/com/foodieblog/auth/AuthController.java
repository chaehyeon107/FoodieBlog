package com.foodieblog.auth;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.auth.dto.AuthResponse;
import com.foodieblog.auth.dto.LoginRequest;
import com.foodieblog.auth.dto.MeResponse;
import com.foodieblog.auth.dto.RefreshRequest;
import com.foodieblog.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(new MeResponse(
                principal.userId(),
                principal.email(),
                principal.nickname(),
                principal.role()
        ));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody com.foodieblog.auth.dto.LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return ApiResponse.ok(null);
    }

}
