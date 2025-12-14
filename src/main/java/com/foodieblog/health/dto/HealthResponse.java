package com.foodieblog.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "헬스 체크 응답")
public class HealthResponse {

    @Schema(description = "서비스 상태", example = "OK")
    private String status;      // OK

    @Schema(description = "애플리케이션 버전", example = "1.0.0")
    private String version;     // app version

    @Schema(description = "빌드 시각 (ISO-8601 또는 unknown)", example = "2025-12-14T13:45:00Z")
    private String buildTime;   // build timestamp

    @Schema(description = "DB 상태 정보")
    private Database database;

    @Getter
    @AllArgsConstructor
    @Schema(description = "데이터베이스 상태")
    public static class Database {

        @Schema(description = "DB 연결 상태", example = "UP")
        private String status;  // UP / DOWN
    }
}
