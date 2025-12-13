package com.foodieblog.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public enum Role { ADMIN, USER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // ✅ 계정 활성/비활성
    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime deactivatedAt;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    @Builder
    public User(String email, String passwordHash, String nickname, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
    }

    public void markLogin() { this.lastLoginAt = LocalDateTime.now(); }

    public void updateNickname(String nickname) { this.nickname = nickname; }

    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void deactivate() {
        this.active = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.deactivatedAt = null;
    }

    public void changeRole(Role role) { this.role = role; }
}
