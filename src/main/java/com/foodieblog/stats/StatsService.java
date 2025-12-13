package com.foodieblog.stats;

import com.foodieblog.comment.CommentRepository;
import com.foodieblog.stats.dto.DailyStatsResponse;
import com.foodieblog.stats.dto.TopAuthorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<DailyStatsResponse> daily(int days) {
        if (days <= 0) days = 7;
        if (days > 90) days = 90; // 과도한 조회 방지

        LocalDateTime from = LocalDate.now().minusDays(days - 1L).atStartOfDay();

        List<Object[]> postRows = statsRepository.countPostsDaily(from);
        List<Object[]> commentRows = commentRepository.countCommentsDaily(from);

        Map<LocalDate, Long> postMap = new HashMap<>();
        for (Object[] r : postRows) {
            LocalDate d = (LocalDate) r[0];
            Long cnt = (Long) r[1];
            postMap.put(d, cnt);
        }

        Map<LocalDate, Long> commentMap = new HashMap<>();
        for (Object[] r : commentRows) {
            LocalDate d = (LocalDate) r[0];
            Long cnt = (Long) r[1];
            commentMap.put(d, cnt);
        }

        List<DailyStatsResponse> out = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = LocalDate.now().minusDays(days - 1L - i);
            out.add(new DailyStatsResponse(
                    d,
                    postMap.getOrDefault(d, 0L),
                    commentMap.getOrDefault(d, 0L)
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<TopAuthorResponse> topAuthors(int days, int limit) {
        if (days <= 0) days = 30;
        if (days > 365) days = 365;
        if (limit <= 0) limit = 10;
        if (limit > 50) limit = 50;

        LocalDateTime from = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        return statsRepository.topAuthors(from, PageRequest.of(0, limit));
    }
}
