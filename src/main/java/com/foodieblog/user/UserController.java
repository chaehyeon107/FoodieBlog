package com.foodieblog.user;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.common.ApiResponse;
import com.foodieblog.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /** 1) 회원가입(공개) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody UserSignupRequest req) {
        return ApiResponse.ok(userService.signup(req));
    }

    /** 2) 내 정보(로그인) */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(userService.me(principal.userId()));
    }

    /** 3) 내 정보 수정(로그인) */
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UserUpdateMeRequest req
    ) {
        return ApiResponse.ok(userService.updateMe(principal.userId(), req));
    }

    /** 4) 비밀번호 변경(로그인) */
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UserChangePasswordRequest req
    ) {
        userService.changePassword(principal.userId(), req);
    }

    /** 5) ADMIN: 유저 목록(검색+페이지) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> adminList(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ApiResponse.ok(userService.adminList(keyword, pageable));
    }

    /** 6) ADMIN: 유저 상세 */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> adminGet(@PathVariable Long id) {
        return ApiResponse.ok(userService.adminGet(id));
    }

    /** 7) ADMIN: 비활성화 */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        userService.adminDeactivate(id);
    }

    /** 8) ADMIN: 활성화 */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Long id) {
        userService.adminActivate(id);
    }

    /** 9) (선택) ADMIN: 역할 변경 */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@PathVariable Long id, @Valid @RequestBody AdminChangeRoleRequest req) {
        userService.adminChangeRole(id, req);
    }
}
