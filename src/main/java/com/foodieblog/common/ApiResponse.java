package com.foodieblog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;

    /** 200 OK */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    /** 201 Created */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data);
    }

    /** 204 No Content */
    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(true, null);
    }
}
