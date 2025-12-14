package com.foodieblog.auth;

import com.foodieblog.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_CODE_ATTR = "AUTH_ERROR_CODE";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7);

        try {
            Claims claims = jwtProvider.parseClaims(token);

            String userId = claims.getSubject();
            String role = (String) claims.get("role");

            String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            var authorities = List.of(new SimpleGrantedAuthority(normalized));

            var principal = new AuthPrincipal(
                    Long.parseLong(userId),
                    (String) claims.get("email"),
                    (String) claims.get("nickname"),
                    role
            );

            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

            System.out.println("[FILTER] " + request.getMethod() + " " + request.getRequestURI()
                    + " auth=" + request.getHeader("Authorization"));


        } catch (JwtProvider.TokenExpiredException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_CODE_ATTR, ErrorCode.TOKEN_EXPIRED);
            chain.doFilter(request, response);

        } catch (JwtProvider.TokenInvalidException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_CODE_ATTR, ErrorCode.UNAUTHORIZED);
            chain.doFilter(request, response);
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }


    public record AuthPrincipal(Long userId, String email, String nickname, String role) {}
}
