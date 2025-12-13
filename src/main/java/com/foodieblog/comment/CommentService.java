package com.foodieblog.comment;

import com.foodieblog.comment.dto.CommentCreateRequest;
import com.foodieblog.comment.dto.CommentResponse;
import com.foodieblog.comment.dto.CommentUpdateRequest;
import com.foodieblog.common.error.BusinessException;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.post.Post;
import com.foodieblog.post.PostRepository;
import com.foodieblog.user.User;
import com.foodieblog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /** 1) 공개 댓글 목록(VISIBLE만) */
    @Transactional(readOnly = true)
    public List<CommentResponse> listVisibleByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return commentRepository
                .findByPost_IdAndStatusOrderByCreatedAtAsc(postId, CommentStatus.VISIBLE)
                .stream().map(CommentResponse::from).toList();
    }

    /** 2) 댓글 작성(로그인) */
    @Transactional
    public CommentResponse create(Long postId, Long authorId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Comment saved = commentRepository.save(Comment.builder()
                .post(post)
                .author(author)
                .content(req.getContent())
                .build());

        return CommentResponse.from(saved);
    }

    /** 3) 댓글 단건 조회(공개: VISIBLE만) */
    @Transactional(readOnly = true)
    public CommentResponse getVisible(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getStatus() == CommentStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.COMMENT_HIDDEN);
        }
        return CommentResponse.from(comment);
    }

    /** 4) 내 댓글(로그인) */
    @Transactional(readOnly = true)
    public Page<CommentResponse> myComments(Long userId, Pageable pageable) {
        return commentRepository.findByAuthor_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(CommentResponse::from);
    }

    /** 5) ADMIN: 댓글 전체 조회 + 필터 */
    @Transactional(readOnly = true)
    public Page<CommentResponse> adminList(Long postId, Long authorId, CommentStatus status, Pageable pageable) {
        Page<Comment> page;

        if (postId != null) {
            page = commentRepository.findByPost_IdOrderByCreatedAtDesc(postId, pageable);
        } else if (authorId != null) {
            page = commentRepository.findByAuthor_UserIdOrderByCreatedAtDesc(authorId, pageable);
        } else if (status != null) {
            page = commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = commentRepository.findAll(pageable);
        }

        return page.map(CommentResponse::from);
    }

    /** 6) ADMIN: 특정 유저 댓글 */
    @Transactional(readOnly = true)
    public Page<CommentResponse> adminByUser(Long userId, Pageable pageable) {
        return commentRepository.findByAuthor_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(CommentResponse::from);
    }

    /** 7) ADMIN: 댓글 수정 */
    @Transactional
    public CommentResponse adminUpdate(Long commentId, CommentUpdateRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        comment.updateContent(req.getContent());
        return CommentResponse.from(comment);
    }

    /** 8) ADMIN: 댓글 삭제 */
    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        commentRepository.delete(comment);
    }

    /** 9) ADMIN: 숨김 */
    @Transactional
    public void adminHide(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        comment.hide();
    }

    /** 10) ADMIN: 표시 */
    @Transactional
    public void adminShow(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        comment.show();
    }
}
