package com.foodieblog.stats;

import com.foodieblog.stats.dto.TopAuthorResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends Repository<com.foodieblog.post.Post, Long> {

    // 일자별 게시글 수
    @Query("""
        select function('date', p.createdAt) as d, count(p)
        from Post p
        where p.createdAt >= :from
        group by function('date', p.createdAt)
        order by d asc
    """)
    List<Object[]> countPostsDaily(@Param("from") LocalDateTime from);

    // Top authors (최근 N일)
    @Query("""
        select new com.foodieblog.stats.dto.TopAuthorResponse(u.userId, u.nickname, count(p))
        from Post p
        join User u on u.userId = p.authorId
        where p.createdAt >= :from
        group by u.userId, u.nickname
        order by count(p) desc
    """)
    List<TopAuthorResponse> topAuthors(@Param("from") LocalDateTime from, Pageable pageable);
}
