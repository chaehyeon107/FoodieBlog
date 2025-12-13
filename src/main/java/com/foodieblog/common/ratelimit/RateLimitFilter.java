package com.foodieblog.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.common.error.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher matcher = new AntPathMatcher();

    // ✅ “인증 없는” 혹은 “공개” 엔드포인트에만 약하게 걸기 (과제 기준 가장 안전)
    private final Set<String> limitedPaths = Set.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/users",   // signup
            "/health"
    );

    // 설정값
    private final int maxRequests;
    private final long windowSeconds;
    private final long maxBodyBytes; // 요청 크기 제한(Content-Length 기준)

    // key: ip|path -> window counter
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxRequests, long windowSeconds, long maxBodyBytes) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.maxBodyBytes = maxBodyBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!shouldLimit(path)) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ 요청 크기 제한 (Content-Length 없으면 -1이므로 통과시킴)
        long contentLength = request.getContentLengthLong();
        if (contentLength > maxBodyBytes) {
            writeError(response, path, ErrorCode.PAYLOAD_TOO_LARGE);
            return;
        }

        // ✅ IP 기반 레이트리밋
        String ip = resolveClientIp(request);
        String key = ip + "|" + path;

        WindowCounter wc = counters.compute(key, (k, old) -> {
            long now = Instant.now().getEpochSecond();
            if (old == null || now - old.windowStartEpochSec >= windowSeconds) {
                return new WindowCounter(now);
            }
            return old;
        });

        int current = wc.count.incrementAndGet();
        if (current > maxRequests) {
            writeError(response, path, ErrorCode.TOO_MANY_REQUESTS);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean shouldLimit(String path) {
        // 정확 매칭 + 확장하고 싶으면 아래 matcher 패턴으로 바꿔도 됨
        if (limitedPaths.contains(path)) return true;

        // 예: 공개 GET에도 걸고 싶으면 여기 확장
        // if (matcher.match("/api/posts/**", path) && "GET".equals(request.getMethod())) ...

        return false;
    }

    private String resolveClientIp(HttpServletRequest request) {
        // 리버스 프록시/로드밸런서 붙을 때 대비
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, String path, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(code, path, Map.of(
                "limit", maxRequests,
                "windowSeconds", windowSeconds
        ));

        objectMapper.writeValue(response.getWriter(), body);
    }

    private static class WindowCounter {
        final long windowStartEpochSec;
        final AtomicInteger count = new AtomicInteger(0);

        WindowCounter(long windowStartEpochSec) {
            this.windowStartEpochSec = windowStartEpochSec;
        }
    }
}
