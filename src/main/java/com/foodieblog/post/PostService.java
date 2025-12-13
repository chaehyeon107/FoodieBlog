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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import static org.springframework.data.jpa.domain.Specification.where;
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

        // ✅ 현재 정책: ADMIN이면 누구 글이든 수정 가능 → author 체크 생략 가능
        // 만약 "관리자도 본인 글만" 정책이면 아래 주석 해제 + ErrorCode 추가 필요
        // if (!post.getAuthorId().equals(actorUserId)) {
        //     throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        // }

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

        // ✅ 현재 정책: ADMIN이면 누구 글이든 삭제 가능 → author 체크 생략 가능
        // if (!post.getAuthorId().equals(actorUserId)) {
        //     throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        // }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> myPosts(Long authorId, Pageable pageable) {
        return postRepository.findAllByAuthorId(authorId, pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> byCategory(Long categoryId, Pageable pageable) {
        // 카테고리 존재 검증을 꼭 하고 싶으면 아래 주석 해제(선택)
        // categoryRepository.findById(categoryId)
        //        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

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

        // ✅ 현재 정책: ADMIN이면 누구 글이든 발행 가능 → author 체크 생략 가능
        post.publish();
    }

    @Transactional
    public void unpublish(Long actorUserId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.unpublish();
    }
    @Transactional(readOnly = true)
    public Page<PostResponse> list(
            String keyword,
            Long categoryId,
            PostStatus status,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo,
            Pageable pageable
    ) {
        var spec = org.springframework.data.jpa.domain.Specification.where(PostSpecifications.keyword(keyword))
                .and(PostSpecifications.categoryId(categoryId))
                .and(PostSpecifications.status(status))
                .and(PostSpecifications.visitedFrom(dateFrom))
                .and(PostSpecifications.visitedTo(dateTo));

        return postRepository.findAll(spec, pageable).map(PostResponse::from);
    }

}
