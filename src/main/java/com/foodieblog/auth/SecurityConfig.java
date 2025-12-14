package com.foodieblog.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.common.error.ErrorResponse;
import com.foodieblog.common.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // ✅ Spring이 관리하는 ObjectMapper를 주입받는다 (JavaTimeModule 포함)
    private final ObjectMapper objectMapper;

    @Value("${ratelimit.max-requests:30}")
    private int rateLimitMax;

    @Value("${ratelimit.window-seconds:60}")
    private long rateLimitWindowSeconds;

    @Value("${request.max-body-bytes:1048576}") // 1MB
    private long maxBodyBytes;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/health/**").permitAll()

                        .requestMatchers("/error").permitAll()

                        .requestMatchers(POST, "/api/auth/login").permitAll()
                        .requestMatchers(POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(POST, "/api/users").permitAll()

                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/*/comments").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/{commentId}").permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )


                 .addFilterBefore(new RateLimitFilter(rateLimitMax, rateLimitWindowSeconds, maxBodyBytes),
                        UsernamePasswordAuthenticationFilter.class)
                 .addFilterBefore(new JwtAuthFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorCode code = (ErrorCode) request.getAttribute(JwtAuthFilter.AUTH_ERROR_CODE_ATTR);
                            if (code == null) code = ErrorCode.UNAUTHORIZED;

                            writeError(response, request.getRequestURI(), code);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            writeError(response, request.getRequestURI(), ErrorCode.FORBIDDEN);
                        })
                );

        return http.build();
    }

    private void writeError(HttpServletResponse response, String path, ErrorCode code) throws IOException {
        // ✅ 이미 응답이 커밋된 상태면 추가로 쓰지 말기 (getWriter() 중복 방지)
        if (response.isCommitted()) return;

        response.resetBuffer(); // ✅ 혹시 남아있던 버퍼 있으면 초기화 (안정성)
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(code, path, null);

        // ✅ new ObjectMapper() 금지 — Spring ObjectMapper 사용
        objectMapper.writeValue(response.getWriter(), body);

        response.flushBuffer(); // ✅ 여기서 확실히 응답 마무리
    }
}
