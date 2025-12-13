package com.foodieblog.health;

import com.foodieblog.common.ApiResponse;
import com.foodieblog.health.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @Value("${app.version:unknown}")
    private String version;

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
