package com.foodieblog.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    Page<User> findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(
            String emailKeyword,
            String nicknameKeyword,
            Pageable pageable
    );
}
