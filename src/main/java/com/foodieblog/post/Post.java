package com.foodieblog.post;

import com.foodieblog.category.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 100)
    private String restaurantName;

    private String address;

    private LocalDate visitedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    /** 작성자(계정) 식별자 - AuthPrincipal.userId() 저장 */
    @Column(nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = PostStatus.DRAFT;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 생성 전용 팩토리 메서드 */
    public static Post create(
            String title,
            String content,
            String restaurantName,
            String address,
            LocalDate visitedAt,
            Category category,
            Long authorId
    ) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.restaurantName = restaurantName;
        post.address = address;
        post.visitedAt = visitedAt;
        post.category = category;
        post.authorId = authorId;
        post.status = PostStatus.DRAFT;
        return post;
    }

    /** 수정 */
    public void update(
            String title,
            String content,
            String restaurantName,
            String address,
            LocalDate visitedAt,
            Category category
    ) {
        this.title = title;
        this.content = content;
        this.restaurantName = restaurantName;
        this.address = address;
        this.visitedAt = visitedAt;
        this.category = category;
    }

    /** 게시(발행) */
    public void publish() {
        this.status = PostStatus.PUBLISHED;
    }

    /** 게시 해제 */
    public void unpublish() {
        this.status = PostStatus.DRAFT;
    }
}
