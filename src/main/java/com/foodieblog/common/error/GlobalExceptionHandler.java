package com.foodieblog.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* =====================
       Validation (@Valid) 실패
       ===================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        // ✅ 어떤 필드가 왜 실패했는지 로그
        log.warn("[VALIDATION] {} {} -> {} details={}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), details);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), details));
    }

    /* =====================
       Query 파라미터 검증 실패
       ===================== */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_QUERY_PARAM;

        // ✅ 어떤 파라미터가 문제인지 원문 로그
        log.warn("[QUERY_PARAM] {} {} -> {} message={}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }

    /* =====================
       비즈니스 예외
       ===================== */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        // ✅ 어떤 비즈니스 에러인지 로그
        log.warn("[BUSINESS] {} {} -> {} message={}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }

    /* =====================
       인증 / 권한
       ===================== */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        log.warn("[AUTH] {} {} -> {} message={}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        log.warn("[FORBIDDEN] {} {} -> {} message={}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }

    /* =====================
       DB 예외
       ===================== */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;

        // ✅ 핵심: DB 예외는 반드시 stacktrace까지 출력
        log.error("[DB] {} {} -> {}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }

    /* =====================
       최종 fallback
       ===================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;

        // ✅ 핵심: UNKNOWN도 stacktrace 출력 (원인 추적용)
        log.error("[UNKNOWN] {} {} -> {}",
                request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI(), null));
    }
}
