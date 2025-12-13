package com.foodieblog.comment;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.comment.dto.CommentCreateRequest;
import com.foodieblog.comment.dto.CommentResponse;
import com.foodieblog.comment.dto.CommentUpdateRequest;
import com.foodieblog.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class CommentController {

    private final CommentService commentService;

    /** 1) 댓글 목록(공개, VISIBLE만) */
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long postId) {
        return ApiResponse.ok(commentService.listVisibleByPost(postId));
    }

    /** 2) 댓글 작성(로그인 USER/ADMIN) */
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        return ApiResponse.ok(commentService.create(postId, principal.userId(), req));
    }

    /** 3) 댓글 단건 조회(공개, VISIBLE만) */
    @GetMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> get(@PathVariable Long commentId) {
        return ApiResponse.ok(commentService.getVisible(commentId));
    }

    /** 4) 내 댓글(로그인) */
    @GetMapping("/comments/me")
    public ApiResponse<Page<CommentResponse>> myComments(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        return ApiResponse.ok(commentService.myComments(principal.userId(), pageable));
    }

    /** 5) ADMIN: 댓글 전체 조회 + 필터 */
    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CommentResponse>> adminList(
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) CommentStatus status,
            Pageable pageable
    ) {
        return ApiResponse.ok(commentService.adminList(postId, authorId, status, pageable));
    }

    /** 6) ADMIN: 특정 유저 댓글 */
    @GetMapping("/comments/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CommentResponse>> byUser(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return ApiResponse.ok(commentService.adminByUser(userId, pageable));
    }

    /** 7) ADMIN: 댓글 수정 */
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CommentResponse> update(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest req
    ) {
        return ApiResponse.ok(commentService.adminUpdate(commentId, req));
    }

    /** 8) ADMIN: 댓글 삭제 */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        commentService.adminDelete(commentId);
    }

    /** 9) ADMIN: 숨김 */
    @PostMapping("/comments/{commentId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable Long commentId) {
        commentService.adminHide(commentId);
    }

    /** 10) ADMIN: 표시 */
    @PostMapping("/comments/{commentId}/show")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void show(@PathVariable Long commentId) {
        commentService.adminShow(commentId);
    }
}
