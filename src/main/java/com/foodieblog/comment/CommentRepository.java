package com.foodieblog.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 공개: VISIBLE만
    List<Comment> findByPost_IdAndStatusOrderByCreatedAtAsc(
            Long postId,
            CommentStatus status
    );

    // USER/ADMIN 공용: 특정 유저의 댓글
    Page<Comment> findByAuthor_UserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // ADMIN: 게시글별
    Page<Comment> findByPost_IdOrderByCreatedAtDesc(
            Long postId,
            Pageable pageable
    );

    // ADMIN: 상태별
    Page<Comment> findByStatusOrderByCreatedAtDesc(
            CommentStatus status,
            Pageable pageable
    );

    @Query("""
    select function('date', c.createdAt) as d, count(c)
    from Comment c
    where c.createdAt >= :from
    group by function('date', c.createdAt)
    order by d asc
""")
    List<Object[]> countCommentsDaily(@Param("from") LocalDateTime from);
}
