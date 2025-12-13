package com.foodieblog.category;

import com.foodieblog.category.dto.CategoryCreateRequest;
import com.foodieblog.category.dto.CategoryResponse;
import com.foodieblog.category.dto.CategoryUpdateRequest;
import com.foodieblog.common.error.BusinessException;
import com.foodieblog.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** 1) 목록(공개) */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll()
                .stream().map(CategoryResponse::from).toList();
    }

    /** 2) 상세(공개) */
    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return CategoryResponse.from(c);
    }

    /** 3) slug 조회(공개) */
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category c = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return CategoryResponse.from(c);
    }

    /** 4) 생성(ADMIN) */
    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        if (categoryRepository.existsByName(req.getName()) || categoryRepository.existsBySlug(req.getSlug())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        Category saved = categoryRepository.save(new Category(req.getName(), req.getSlug()));
        return CategoryResponse.from(saved);
    }

    /** 5) 수정(ADMIN) */
    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByNameAndIdNot(req.getName(), id)
                || categoryRepository.existsBySlugAndIdNot(req.getSlug(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        c.update(req.getName(), req.getSlug());
        return CategoryResponse.from(c);
    }

    /** 6) 삭제(ADMIN) */
    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(c);
    }
}
