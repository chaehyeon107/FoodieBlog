package com.foodieblog.health;

import com.foodieblog.common.ApiResponse;
import com.foodieblog.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Tag(name = "Health", description = "서버 상태 및 의존성(DB) 헬스 체크 API")
public class HealthController {

    private final HealthService healthService;

    @Value("${app.version:unknown}")
    private String version;

    @Operation(
            summary = "헬스 체크",
            description = """
                    서버 및 데이터베이스 상태를 확인합니다.
                    
                    - 인증 불필요
                    - JCloud 헬스체크 및 배포 검증용
                    - DB 연결 상태 포함
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "서버 정상 동작",
                    content = @Content(
                            schema = @Schema(implementation = HealthResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            schema = @Schema(implementation = com.foodieblog.common.error.ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        String buildTime = readBuildTimeOrUnknown();
        boolean dbUp = healthService.isDatabaseUp();

        HealthResponse response = new HealthResponse(
                "OK",
                version,
                buildTime,
                new HealthResponse.Database(dbUp ? "UP" : "DOWN")
        );

        return ApiResponse.ok(response);
    }

    private String readBuildTimeOrUnknown() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("build-info/build-time.txt");

            if (!resource.exists()) return "unknown";

            return StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            ).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
