package com.foodieblog.post;

import com.foodieblog.auth.JwtAuthFilter.AuthPrincipal;
import com.foodieblog.common.ApiResponse;
import com.foodieblog.post.dto.PostCreateRequest;
import com.foodieblog.post.dto.PostResponse;
import com.foodieblog.post.dto.PostUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;




@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;


    @GetMapping
    public ApiResponse<Page<PostResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo,
            Pageable pageable
    ) {
        return ApiResponse.ok(postService.list(keyword, categoryId, status, dateFrom, dateTo, pageable));
    }


    @GetMapping("/{id}")
    public ApiResponse<PostResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(postService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PostResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody PostCreateRequest req
    ) {
        return ApiResponse.ok(postService.create(principal.userId(), req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PostResponse> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest req
    ) {
        return ApiResponse.ok(postService.update(principal.userId(), id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id
    ) {
        postService.delete(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PostResponse>> myPosts(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        return ApiResponse.ok(postService.myPosts(principal.userId(), pageable));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> publish(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id
    ) {
        postService.publish(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unpublish(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id
    ) {
        postService.unpublish(principal.userId(), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/category/{id}")
    public ApiResponse<Page<PostResponse>> byCategory(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return ApiResponse.ok(postService.byCategory(id, pageable));
    }

    @GetMapping("/user/{id}")
    public ApiResponse<Page<PostResponse>> byUser(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return ApiResponse.ok(postService.byUser(id, pageable));
    }
}
