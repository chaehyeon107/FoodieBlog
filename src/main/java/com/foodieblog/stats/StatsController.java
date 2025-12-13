package com.foodieblog.stats;

import com.foodieblog.common.ApiResponse;
import com.foodieblog.stats.dto.DailyStatsResponse;
import com.foodieblog.stats.dto.TopAuthorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DailyStatsResponse>> daily(@RequestParam(defaultValue = "14") int days) {
        return ApiResponse.ok(statsService.daily(days));
    }

    @GetMapping("/top-authors")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TopAuthorResponse>> topAuthors(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(statsService.topAuthors(days, limit));
    }
}
