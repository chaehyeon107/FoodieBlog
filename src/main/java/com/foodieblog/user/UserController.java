package com.foodieblog.user;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.common.ApiResponse;
import com.foodieblog.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "사용자 API (회원가입/내 정보/비밀번호 변경 + 관리자 유저 관리)")
public class UserController {

    private final UserService userService;

    /** 1) 회원가입(공개) */
    @Operation(
            summary = "회원가입 (공개)",
            description = """
                    회원가입을 진행합니다. (공개 API)
                    - 성공 시 201(CREATED)
                    - 이메일/닉네임 중복은 409(CONFLICT)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공 (Created)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 (VALIDATION_FAILED, BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복 데이터 (EMAIL_ALREADY_EXISTS, NICKNAME_ALREADY_EXISTS, DUPLICATE_RESOURCE)",
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody UserSignupRequest req) {
        return ApiResponse.ok(userService.signup(req));
    }

    /** 2) 내 정보(로그인) */
    @Operation(
            summary = "내 정보 조회 (로그인)",
            description = "현재 로그인한 사용자의 정보를 조회합니다. (JWT 필요)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "비활성 계정/권한 없음 (USER_DEACTIVATED, FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(userService.me(principal.userId()));
    }

    /** 3) 내 정보 수정(로그인) */
    @Operation(
            summary = "내 정보 수정 (로그인)",
            description = """
                    로그인한 사용자의 프로필 정보를 수정합니다. (JWT 필요)
                    - 예: 닉네임 변경 등
                    - 닉네임 중복 시 409(CONFLICT)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
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
                    responseCode = "403",
                    description = "비활성 계정/권한 없음 (USER_DEACTIVATED, FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복 닉네임 (NICKNAME_ALREADY_EXISTS, DUPLICATE_RESOURCE)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UserUpdateMeRequest req
    ) {
        return ApiResponse.ok(userService.updateMe(principal.userId(), req));
    }

    /** 4) 비밀번호 변경(로그인) */
    @Operation(
            summary = "비밀번호 변경 (로그인)",
            description = """
                    로그인한 사용자의 비밀번호를 변경합니다. (JWT 필요)
                    - 성공 시 204(No Content)
                    - 현재 비밀번호 불일치 등의 논리 오류는 422(UNPROCESSABLE_ENTITY)로 문서화
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "변경 성공 (No Content)"),
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
                    responseCode = "403",
                    description = "비활성 계정/권한 없음 (USER_DEACTIVATED, FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "처리 불가 (UNPROCESSABLE_ENTITY) - 예: 현재 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UserChangePasswordRequest req
    ) {
        userService.changePassword(principal.userId(), req);
    }

    /** 5) ADMIN: 유저 목록(검색+페이지) */
    @Operation(
            summary = "유저 목록 조회 (관리자)",
            description = """
                    관리자(ROLE_ADMIN) 전용: 유저 목록을 조회합니다.
                    
                    ✅ 검색:
                    - keyword: 이메일/닉네임 등 프로젝트 기준 검색
                    
                    ✅ 페이지네이션/정렬:
                    - page, size, sort (Spring Pageable)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "쿼리 파라미터 오류 (INVALID_QUERY_PARAM, BAD_REQUEST)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> adminList(
            @Parameter(description = "검색 키워드(이메일/닉네임 등)", example = "user1")
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(userService.adminList(keyword, pageable));
    }

    /** 6) ADMIN: 유저 상세 */
    @Operation(
            summary = "유저 상세 조회 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: userId로 유저 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> adminGet(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.ok(userService.adminGet(id));
    }

    /** 7) ADMIN: 비활성화 */
    @Operation(
            summary = "유저 비활성화 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 유저를 비활성화합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "비활성화 성공 (No Content)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "상태 충돌 (STATE_CONFLICT) - 예: 이미 비활성 상태",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        userService.adminDeactivate(id);
    }

    /** 8) ADMIN: 활성화 */
    @Operation(
            summary = "유저 활성화 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 유저를 활성화합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "활성화 성공 (No Content)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패/토큰 만료 (UNAUTHORIZED, TOKEN_EXPIRED)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "상태 충돌 (STATE_CONFLICT) - 예: 이미 활성 상태",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        userService.adminActivate(id);
    }

    /** 9) (선택) ADMIN: 역할 변경 */
    @Operation(
            summary = "유저 역할 변경 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 유저의 권한(role)을 변경합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "역할 변경 성공 (No Content)"),
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
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "상태 충돌 (STATE_CONFLICT)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AdminChangeRoleRequest req
    ) {
        userService.adminChangeRole(id, req);
    }
}
