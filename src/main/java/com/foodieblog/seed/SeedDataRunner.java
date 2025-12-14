package com.foodieblog.seed;

import com.foodieblog.category.Category;
import com.foodieblog.category.CategoryRepository;
import com.foodieblog.comment.Comment;
import com.foodieblog.comment.CommentRepository;
import com.foodieblog.post.Post;
import com.foodieblog.post.PostRepository;
import com.foodieblog.user.User;
import com.foodieblog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class SeedDataRunner {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Bean
    CommandLineRunner seedFoodBlogData() {
        return args -> seed();
    }

    @Transactional
    public void seed() {

        // 이미 충분하면 스킵 (중복 방지)
        if (postRepository.count() >= 100 && commentRepository.count() >= 120) {
            return;
        }

        // 🔐 기본 유저 (이미 seed 되어 있다고 가정)
        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new IllegalStateException("admin user missing"));
        User user = userRepository.findByEmail("user@example.com")
                .orElseThrow(() -> new IllegalStateException("user user missing"));

        // 🍽️ 1. 카테고리 (맛집 테마)
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("한식", "korean"),
                    new Category("중식", "chinese"),
                    new Category("일식", "japanese"),
                    new Category("양식", "western"),
                    new Category("카페", "cafe"),
                    new Category("술집", "pub"),
                    new Category("분식", "street-food"),
                    new Category("패스트푸드", "fast-food")
            ));
        }

        List<Category> categories = categoryRepository.findAll();

        // 🍜 샘플 식당명 풀
        String[] restaurantNames = {
                "전주식당", "고궁비빔밥", "삼백집", "교동짬뽕",
                "스시하루", "멘야산다이메", "카페온유",
                "브루클린버거", "미분당", "이태리부엌"
        };

        String[] addresses = {
                "전북 전주시 완산구",
                "전북 전주시 덕진구",
                "전북 전주시 효자동",
                "전북 전주시 객사"
        };

        String[] reviewTemplates = {
                "음식이 정말 깔끔하고 맛있었습니다.",
                "웨이팅이 있었지만 기다릴 가치가 있었어요.",
                "재방문 의사 100%입니다.",
                "가격 대비 만족도가 높아요.",
                "분위기가 좋아서 데이트 장소로 추천합니다."
        };

        Random r = new Random();

        // 📝 2. 게시글 100개 (ADMIN = 블로그 주인)
        if (postRepository.count() < 100) {
            for (int i = 1; i <= 100; i++) {
                Category category = categories.get(r.nextInt(categories.size()));
                String restaurant = restaurantNames[r.nextInt(restaurantNames.length)];
                String address = addresses[r.nextInt(addresses.length)];
                String review = reviewTemplates[r.nextInt(reviewTemplates.length)];

                Post post = Post.create(
                        restaurant + " 방문 후기",
                        restaurant + "에 다녀왔습니다. " + review,
                        restaurant,
                        address,
                        LocalDate.now().minusDays(r.nextInt(180)),
                        category,
                        admin.getUserId()
                );

                // 절반은 공개
                if (i % 2 == 0) {
                    post.publish();
                }

                postRepository.save(post);
            }
        }

        List<Post> posts = postRepository.findAll();

        // 💬 3. 댓글 120개 (USER = 방문자)
        if (commentRepository.count() < 120) {
            for (int i = 1; i <= 120; i++) {
                Post post = posts.get(r.nextInt(posts.size()));

                Comment comment = Comment.builder()
                        .post(post)
                        .author(user)
                        .content(
                                i % 3 == 0
                                        ? "여기 주차는 편한가요?"
                                        : i % 5 == 0
                                        ? "사진보다 실제가 더 맛있어 보이네요!"
                                        : "후기 보고 방문해보고 싶어요 👍"
                        )
                        .build();

                // 일부 댓글 숨김 처리 (관리자 기능 검증용)
                if (i % 10 == 0) {
                    comment.hide();
                }

                commentRepository.save(comment);
            }
        }

        // ✅ 총합:
        // Category: 8
        // Post: 100
        // Comment: 120
        // User: 기존 seed
        // → 228건 이상 (요구사항 충족)
    }
}
