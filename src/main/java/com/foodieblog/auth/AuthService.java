package com.foodieblog.auth;

import com.foodieblog.auth.dto.AuthResponse;
import com.foodieblog.auth.dto.LoginRequest;
import com.foodieblog.common.error.BusinessException;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.user.User;
import com.foodieblog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail();
        String password = req.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // (선택) 로그인 시간 기록하고 싶으면
        user.markLogin();

        String accessToken = jwtProvider.generateAccessToken(
                user.getUserId(),
                user.getRole().name(),
                user.getEmail(),
                user.getNickname()
        );

        // 1유저 1토큰 전략
        refreshTokenRepository.deleteByUserId(user.getUserId());

        String refreshTokenValue = jwtProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiryAt(jwtProvider.refreshTokenExpiryAt())
                .build();

        refreshTokenRepository.save(refreshToken);

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );

        return AuthResponse.forLogin(accessToken, refreshTokenValue, userDto);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        String newAccessToken = jwtProvider.generateAccessToken(
                user.getUserId(),
                user.getRole().name(),
                user.getEmail(),
                user.getNickname()
        );

        return AuthResponse.forRefresh(newAccessToken);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

}
