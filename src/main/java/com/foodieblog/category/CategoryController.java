package com.foodieblog.category;

import com.foodieblog.category.dto.CategoryCreateRequest;
import com.foodieblog.category.dto.CategoryResponse;
import com.foodieblog.category.dto.CategoryUpdateRequest;
import com.foodieblog.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 1) 목록(공개) */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.list());
    }

    /** 2) 상세(공개) */
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.get(id));
    }

    /** 3) slug 조회(공개) */
    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(categoryService.getBySlug(slug));
    }

    /** 4) 생성(ADMIN) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ApiResponse.ok(categoryService.create(req));
    }

    /** 5) 수정(ADMIN) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest req
    ) {
        return ApiResponse.ok(categoryService.update(id, req));
    }

    /** 6) 삭제(ADMIN) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
