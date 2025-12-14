package com.foodieblog;

import com.foodieblog.user.User;
import com.foodieblog.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;


@EntityScan("com.foodieblog")
@EnableJpaRepositories("com.foodieblog")
@SpringBootApplication
public class FoodieblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodieblogApplication.class, args);
	}

	@Bean
	CommandLineRunner debugEnv(Environment env) {
		return args -> {
			System.out.println("[DEBUG] spring.datasource.url=" + env.getProperty("spring.datasource.url"));
			System.out.println("[DEBUG] DB_URL=" + env.getProperty("DB_URL"));
		};
	}

	@Bean
	CommandLineRunner seedUsers(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			// admin
			@Value("${seed.admin.email}") String adminEmail,
			@Value("${seed.admin.password}") String adminPassword,
			@Value("${seed.admin.nickname}") String adminNickname,
			// user
			@Value("${seed.user.email}") String userEmail,
			@Value("${seed.user.password}") String userPassword,
			@Value("${seed.user.nickname}") String userNickname
	) {
		return args -> {
			if (!userRepository.existsByEmail(adminEmail)) {
				userRepository.save(User.builder()
						.email(adminEmail)
						.passwordHash(passwordEncoder.encode(adminPassword))
						.nickname(adminNickname)
						.role(User.Role.ADMIN)
						.build());
			}

			if (!userRepository.existsByEmail(userEmail)) {
				userRepository.save(User.builder()
						.email(userEmail)
						.passwordHash(passwordEncoder.encode(userPassword))
						.nickname(userNickname)
						.role(User.Role.USER)
						.build());
			}
		};
	}
}
