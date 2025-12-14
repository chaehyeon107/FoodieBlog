package com.foodieblog.post;

import com.foodieblog.category.Category;
import com.foodieblog.category.CategoryRepository;
import com.foodieblog.common.error.BusinessException;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.post.dto.PostCreateRequest;
import com.foodieblog.post.dto.PostResponse;
import com.foodieblog.post.dto.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> list(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse create(Long authorId, PostCreateRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Post post = Post.create(
                req.getTitle(),
                req.getContent(),
                req.getRestaurantName(),
                req.getAddress(),
                req.getVisitedAt(),
                category,
                authorId
        );

        Post saved = postRepository.save(post);
        return PostResponse.from(saved);
    }

    @Transactional
    public PostResponse update(Long actorUserId, Long postId, PostUpdateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        post.update(
                req.getTitle(),
                req.getContent(),
                req.getRestaurantName(),
                req.getAddress(),
                req.getVisitedAt(),
                category
        );

        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long actorUserId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> myPosts(Long authorId, Pageable pageable) {
        return postRepository.findAllByAuthorId(authorId, pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> byCategory(Long categoryId, Pageable pageable) {
        return postRepository.findAllByCategory_Id(categoryId, pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> byUser(Long userId, Pageable pageable) {
        return postRepository.findAllByAuthorId(userId, pageable).map(PostResponse::from);
    }

    @Transactional
    public void publish(Long actorUserId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        post.publish();
    }

    @Transactional
    public void unpublish(Long actorUserId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        post.unpublish();
    }

    /**
     * ✅ 필터 검색(list) - Specification null 문제 해결 버전
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> list(
            String keyword,
            Long categoryId,
            PostStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    ) {
        // ✅ 항상 true인 spec으로 시작 (null 방지)
        Specification<Post> spec = alwaysTrue();

        // ✅ 각 스펙이 null일 수 있으니 safeSpec으로 감싸서 and
        spec = spec.and(safeSpec(PostSpecifications.keyword(keyword)));
        spec = spec.and(safeSpec(PostSpecifications.categoryId(categoryId)));
        spec = spec.and(safeSpec(PostSpecifications.status(status)));
        spec = spec.and(safeSpec(PostSpecifications.visitedFrom(dateFrom)));
        spec = spec.and(safeSpec(PostSpecifications.visitedTo(dateTo)));

        return postRepository.findAll(spec, pageable).map(PostResponse::from);
    }

    /** null이면 (1=1) 스펙으로 대체 */
    private Specification<Post> safeSpec(Specification<Post> s) {
        return (s == null) ? alwaysTrue() : s;
    }

    /** 항상 true (where절 1=1) */
    private Specification<Post> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }
}
