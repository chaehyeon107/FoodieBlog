package com.foodieblog.health.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthResponse {

    private String status;      // OK
    private String version;     // app version
    private String buildTime;   // build timestamp
    private Database database;

    @Getter
    @AllArgsConstructor
    public static class Database {
        private String status;  // UP / DOWN
    }
}
