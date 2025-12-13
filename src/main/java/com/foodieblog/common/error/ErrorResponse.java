package com.foodieblog.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private Instant timestamp;
    private String path;
    private int status;
    private String code;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
