package com.foodieblog.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatsResponse {
    private LocalDate date;
    private long postCount;
    private long commentCount;
}
