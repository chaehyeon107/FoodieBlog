package com.foodieblog.auth;

import com.foodieblog.common.ratelimit.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;


// ... 생략 (imports 동일)

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

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
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http

                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(POST, "/api/auth/login").permitAll()
                        .requestMatchers(POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/health").permitAll() // ✅ 헬스체크 공개

                        .requestMatchers(POST, "/api/users").permitAll()   // ✅ 회원가입 공개

                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/*/comments").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/{commentId}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new RateLimitFilter(rateLimitMax, rateLimitWindowSeconds, maxBodyBytes),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorCode code = (ErrorCode) request.getAttribute(JwtAuthFilter.AUTH_ERROR_CODE_ATTR);
                            if (code == null) code = ErrorCode.UNAUTHORIZED;
                            writeError(response, request.getRequestURI(), code);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            writeError(response, request.getRequestURI(), ErrorCode.FORBIDDEN);
                        })
                )
                .build();
    }

    private void writeError(HttpServletResponse response, String path, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = ErrorResponse.of(code, path, null);
        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}
