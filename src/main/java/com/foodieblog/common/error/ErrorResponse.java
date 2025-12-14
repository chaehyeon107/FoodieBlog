package com.foodieblog.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@Schema(description = "공통 에러 응답 포맷")
public class ErrorResponse {

    @Schema(
            description = "에러 발생 시각 (ISO-8601)",
            example = "2025-12-14T12:34:56Z"
    )
    private Instant timestamp;

    @Schema(
            description = "요청 경로",
            example = "/api/auth/login"
    )
    private String path;

    @Schema(
            description = "HTTP 상태 코드",
            example = "401"
    )
    private int status;

    @Schema(
            description = "시스템 내부 에러 코드",
            example = "TOKEN_EXPIRED"
    )
    private String code;

    @Schema(
            description = "사용자에게 노출되는 에러 메시지",
            example = "인증 토큰이 만료되었습니다."
    )
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(
            description = "필드별 오류 상세 정보 (Validation 실패 등)",
            example = "{\"email\": \"INVALID_EMAIL_FORMAT\"}"
    )
    private Map<String, Object> details;

    public static ErrorResponse of(
            ErrorCode errorCode,
            String path,
            Map<String, Object> details
    ) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .path(path)
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build();
    }
}
