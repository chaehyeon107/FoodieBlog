package com.foodieblog.category;

import com.foodieblog.category.dto.CategoryCreateRequest;
import com.foodieblog.category.dto.CategoryResponse;
import com.foodieblog.category.dto.CategoryUpdateRequest;
import com.foodieblog.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "카테고리 API (공개 조회 + 관리자 생성/수정/삭제)")
public class CategoryController {

    private final CategoryService categoryService;

    /** 1) 목록(공개) */
    @Operation(
            summary = "카테고리 목록 조회 (공개)",
            description = "카테고리 전체 목록을 조회합니다. (공개 API)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.list());
    }

    /** 2) 상세(공개) */
    @Operation(
            summary = "카테고리 상세 조회 (공개)",
            description = "카테고리 ID로 상세 정보를 조회합니다. (공개 API)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "카테고리 없음 (CATEGORY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.ok(categoryService.get(id));
    }

    /** 3) slug 조회(공개) */
    @Operation(
            summary = "카테고리 slug 조회 (공개)",
            description = "카테고리 slug로 상세 정보를 조회합니다. (공개 API)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "카테고리 없음 (CATEGORY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (INTERNAL_SERVER_ERROR, DATABASE_ERROR, UNKNOWN_ERROR)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            )
    })
    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryResponse> getBySlug(
            @Parameter(description = "카테고리 slug", example = "korean-food")
            @PathVariable String slug
    ) {
        return ApiResponse.ok(categoryService.getBySlug(slug));
    }

    /** 4) 생성(ADMIN) */
    @Operation(
            summary = "카테고리 생성 (관리자)",
            description = "관리자(ROLE_ADMIN)만 카테고리를 생성할 수 있습니다. 성공 시 201(CREATED)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공 (Created)"
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
                    responseCode = "409",
                    description = "중복 리소스 (DUPLICATE_RESOURCE) - 예: slug/name 중복",
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
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ApiResponse.ok(categoryService.create(req));
    }

    /** 5) 수정(ADMIN) */
    @Operation(
            summary = "카테고리 수정 (관리자)",
            description = "관리자(ROLE_ADMIN)만 카테고리를 수정할 수 있습니다."
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
                    description = "카테고리 없음 (CATEGORY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복/상태 충돌 (DUPLICATE_RESOURCE, STATE_CONFLICT)",
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
    public ApiResponse<CategoryResponse> update(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest req
    ) {
        return ApiResponse.ok(categoryService.update(id, req));
    }

    /** 6) 삭제(ADMIN) */
    @Operation(
            summary = "카테고리 삭제 (관리자)",
            description = "관리자(ROLE_ADMIN)만 카테고리를 삭제할 수 있습니다. 성공 시 204(No Content)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공 (No Content)"
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
                    description = "카테고리 없음 (CATEGORY_NOT_FOUND)",
                    content = @Content(schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "상태 충돌 (STATE_CONFLICT) - 예: 삭제 불가 상태",
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long id
    ) {
        categoryService.delete(id);
    }
}
