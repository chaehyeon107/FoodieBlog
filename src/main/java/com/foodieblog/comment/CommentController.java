package com.foodieblog.comment;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.comment.dto.CommentCreateRequest;
import com.foodieblog.comment.dto.CommentResponse;
import com.foodieblog.comment.dto.CommentUpdateRequest;
import com.foodieblog.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Comments", description = "댓글 API (공개 조회/작성 + 관리자 관리/숨김/표시)")
public class CommentController {

    private final CommentService commentService;

    /** 1) 댓글 목록(공개, VISIBLE만) */
    @Operation(
            summary = "게시글 댓글 목록 조회 (공개)",
            description = "특정 게시글의 댓글 목록을 조회합니다. 공개 API이며 VISIBLE 상태 댓글만 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (POST_NOT_FOUND) 또는 리소스 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> list(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        return ApiResponse.ok(commentService.listVisibleByPost(postId));
    }

    /** 2) 댓글 작성(로그인 USER/ADMIN) */
    @Operation(
            summary = "댓글 작성 (로그인)",
            description = """
                    특정 게시글에 댓글을 작성합니다. (JWT 필요)
                    - 일반 사용자(ROLE_USER) 및 관리자(ROLE_ADMIN) 모두 작성 가능
                    - 성공 시 201(CREATED)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "작성 성공"
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
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        return ApiResponse.ok(commentService.create(postId, principal.userId(), req));
    }

    /** 3) 댓글 단건 조회(공개, VISIBLE만) */
    @Operation(
            summary = "댓글 단건 조회 (공개)",
            description = "댓글 ID로 단건을 조회합니다. 공개 API이며 VISIBLE 상태 댓글만 조회됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "숨김 댓글 접근 (COMMENT_HIDDEN) 또는 권한 없음 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 (COMMENT_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> get(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId
    ) {
        return ApiResponse.ok(commentService.getVisible(commentId));
    }

    /** 4) 내 댓글(로그인) */
    @Operation(
            summary = "내 댓글 목록 조회 (로그인)",
            description = """
                    로그인한 사용자의 댓글 목록을 페이지네이션으로 조회합니다. (JWT 필요)
                    - page/size/sort 사용 가능
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
    @GetMapping("/comments/me")
    public ApiResponse<Page<CommentResponse>> myComments(
            @AuthenticationPrincipal AuthPrincipal principal,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(commentService.myComments(principal.userId(), pageable));
    }

    /** 5) ADMIN: 댓글 전체 조회 + 필터 */
    @Operation(
            summary = "댓글 전체 조회 (관리자)",
            description = """
                    관리자(ROLE_ADMIN) 전용 댓글 조회 API입니다.
                    
                    ✅ 필터:
                    - postId: 특정 게시글 댓글만
                    - authorId: 특정 작성자 댓글만
                    - status: 댓글 상태 (예: VISIBLE/HIDDEN 등 프로젝트 정의)
                    
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
    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CommentResponse>> adminList(
            @Parameter(description = "게시글 ID 필터", example = "1")
            @RequestParam(required = false) Long postId,

            @Parameter(description = "작성자 userId 필터", example = "1")
            @RequestParam(required = false) Long authorId,

            @Parameter(description = "댓글 상태 필터 (예: VISIBLE, HIDDEN)", example = "VISIBLE")
            @RequestParam(required = false) CommentStatus status,

            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(commentService.adminList(postId, authorId, status, pageable));
    }

    /** 6) ADMIN: 특정 유저 댓글 */
    @Operation(
            summary = "특정 사용자 댓글 목록 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 특정 사용자(userId)의 댓글 목록을 페이지네이션으로 조회합니다."
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
    @GetMapping("/comments/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CommentResponse>> byUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.ok(commentService.adminByUser(userId, pageable));
    }

    /** 7) ADMIN: 댓글 수정 */
    @Operation(
            summary = "댓글 수정 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 댓글 내용을 수정합니다."
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
                    description = "관리자 권한 필요 (FORBIDDEN)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 (COMMENT_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CommentResponse> update(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest req
    ) {
        return ApiResponse.ok(commentService.adminUpdate(commentId, req));
    }

    /** 8) ADMIN: 댓글 삭제 */
    @Operation(
            summary = "댓글 삭제 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 댓글을 삭제합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
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
                    description = "댓글 없음 (COMMENT_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId
    ) {
        commentService.adminDelete(commentId);
    }

    /** 9) ADMIN: 숨김 */
    @Operation(
            summary = "댓글 숨김 처리 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 댓글을 숨김(HIDDEN) 처리합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "숨김 처리 성공 (No Content)"),
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
                    description = "댓글 없음 (COMMENT_NOT_FOUND)",
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
    @PostMapping("/comments/{commentId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId
    ) {
        commentService.adminHide(commentId);
    }

    /** 10) ADMIN: 표시 */
    @Operation(
            summary = "댓글 표시 처리 (관리자)",
            description = "관리자(ROLE_ADMIN) 전용: 댓글을 표시(VISIBLE) 처리합니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "표시 처리 성공 (No Content)"),
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
                    description = "댓글 없음 (COMMENT_NOT_FOUND)",
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
    @PostMapping("/comments/{commentId}/show")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void show(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId
    ) {
        commentService.adminShow(commentId);
    }
}
