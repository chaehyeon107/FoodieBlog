package com.foodieblog.user;

import com.foodieblog.common.error.BusinessException;
import com.foodieblog.common.error.ErrorCode;
import com.foodieblog.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 1) 회원가입 */
    @Transactional
    public UserResponse signup(UserSignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User saved = userRepository.save(User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .role(User.Role.USER)
                .build());

        return UserResponse.from(saved);
    }

    /** 2) 내 정보 */
    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_DEACTIVATED);
        }
        return UserResponse.from(user);
    }

    /** 3) 내 정보 수정(닉네임) */
    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateMeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) throw new BusinessException(ErrorCode.USER_DEACTIVATED);

        if (!user.getNickname().equals(req.getNickname()) && userRepository.existsByNickname(req.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.updateNickname(req.getNickname());
        return UserResponse.from(user);
    }

    /** 4) 비밀번호 변경 */
    @Transactional
    public void changePassword(Long userId, UserChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) throw new BusinessException(ErrorCode.USER_DEACTIVATED);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        user.changePasswordHash(passwordEncoder.encode(req.getNewPassword()));
    }

    /** 5) ADMIN: 유저 목록(검색 + 페이지) */
    @Transactional(readOnly = true)
    public Page<UserResponse> adminList(String keyword, Pageable pageable) {
        Page<User> page;
        if (keyword == null || keyword.isBlank()) {
            page = userRepository.findAll(pageable);
        } else {
            page = userRepository.findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        }
        return page.map(UserResponse::from);
    }

    /** 6) ADMIN: 유저 상세 */
    @Transactional(readOnly = true)
    public UserResponse adminGet(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    /** 7) ADMIN: 비활성화 */
    @Transactional
    public void adminDeactivate(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.deactivate();
    }

    /** 8) ADMIN: 활성화 */
    @Transactional
    public void adminActivate(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.activate();
    }

    /** 9) (선택) ADMIN: 역할 변경 */
    @Transactional
    public void adminChangeRole(Long targetUserId, AdminChangeRoleRequest req) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        try {
            User.Role role = User.Role.valueOf(req.getRole());
            user.changeRole(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
