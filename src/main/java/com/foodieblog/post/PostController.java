package com.foodieblog.post;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.common.ApiResponse;
import com.foodieblog.post.dto.PostCreateRequest;
import com.foodieblog.post.dto.PostResponse;
import com.foodieblog.post.dto.PostUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "게시글 API (목록/상세/검색/정렬/페이지네이션 + 관리자 작성/수정/삭제/발행)")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "게시글 목록 조회",
            description = """
                    게시글 목록을 조회합니다. (공개 API)
                    
                    ✅ 지원 기능:
                    - 검색: keyword
                    - 필터: categoryId, status, dateFrom/dateTo
                    - 페이지네이션/정렬: page, size, sort (Spring Pageable)
                    
                    예) /api/posts?keyword=전북대&categoryId=1&status=PUBLISHED&page=0&size=10&sort=createdAt,DESC
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공"
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
    public ApiResponse<Page<PostResponse>> list(
            @Parameter(description = "검색 키워드 (제목/본문/가게명 등 프로젝트 기준)", example = "전북대")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "카테고리 ID 필터", example = "1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "게시글 상태 필터 (예: DRAFT, PUBLISHED)", example = "PUBLISHED")
            @RequestParam(required = false) PostStatus status,

            @Parameter(description = "작성일 시작(YYYY-MM-DD)", example = "2025-12-01")
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,

            @Parameter(description = "작성일 종료(YYYY-MM-DD)", example = "2025-12-14")
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo,

            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(postService.list(keyword, categoryId, status, dateFrom, dateTo, pageable));
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 상세 정보를 조회합니다. (공개 API)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (POST_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<PostResponse> detail(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.ok(postService.get(id));
    }

    @Operation(
            summary = "게시글 생성 (관리자)",
            description = "관리자(ROLE_ADMIN)만 게시글을 생성할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 성공"
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
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "카테고리 없음 등 리소스 없음 (CATEGORY_NOT_FOUND, RESOURCE_NOT_FOUND)",
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
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PostResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody PostCreateRequest req
    ) {
        return ApiResponse.ok(postService.create(principal.userId(), req));
    }

    @Operation(
            summary = "게시글 수정 (관리자)",
            description = "관리자(ROLE_ADMIN)만 게시글을 수정할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
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
                    responseCode = "403",
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (POST_NOT_FOUND)",
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
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PostResponse> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest req
    ) {
        return ApiResponse.ok(postService.update(principal.userId(), id, req));
    }

    @Operation(
            summary = "게시글 삭제 (관리자)",
            description = "관리자(ROLE_ADMIN)만 게시글을 삭제할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
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
                    description = "게시글 없음 (POST_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "요청 한도 초과 (TOO_MANY_REQUESTS)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id
    ) {
        postService.delete(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @Operation(
            summary = "내가 작성한 게시글 목록 (관리자)",
            description = "관리자 본인이 작성한 게시글 목록을 조회합니다. (JWT 필요)"
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
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PostResponse>> myPosts(
            @AuthenticationPrincipal AuthPrincipal principal,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(postService.myPosts(principal.userId(), pageable));
    }

    @Operation(
            summary = "게시글 발행 (관리자)",
            description = "관리자(ROLE_ADMIN)만 게시글을 발행(PUBLISHED) 상태로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발행 성공"),
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
                    description = "게시글 없음 (POST_NOT_FOUND)",
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
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publish(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id
    ) {
        postService.publish(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @Operation(
            summary = "게시글 발행 취소 (관리자)",
            description = "관리자(ROLE_ADMIN)만 게시글을 비발행(DRAFT 등) 상태로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발행 취소 성공"),
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
                    description = "게시글 없음 (POST_NOT_FOUND)",
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
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unpublish(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id
    ) {
        postService.unpublish(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @Operation(
            summary = "카테고리별 게시글 목록 조회",
            description = "카테고리 ID로 게시글 목록을 조회합니다. (공개 API, 페이지네이션 지원)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "카테고리 없음 (CATEGORY_NOT_FOUND)",
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
    @GetMapping("/category/{id}")
    public ApiResponse<Page<PostResponse>> byCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long id,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(postService.byCategory(id, pageable));
    }

    @Operation(
            summary = "작성자별 게시글 목록 조회",
            description = "작성자(userId)로 게시글 목록을 조회합니다. (공개 API, 페이지네이션 지원)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_NOT_FOUND)",
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
    @GetMapping("/user/{id}")
    public ApiResponse<Page<PostResponse>> byUser(
            @Parameter(description = "작성자 userId", example = "1")
            @PathVariable Long id,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(postService.byUser(id, pageable));
    }
}
