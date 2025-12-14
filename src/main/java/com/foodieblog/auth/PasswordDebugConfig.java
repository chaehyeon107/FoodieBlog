package com.foodieblog.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordDebugConfig {

    @Bean
    CommandLineRunner printPasswordHash(PasswordEncoder encoder) {
        return args -> {
            System.out.println("admin1234 = " + encoder.encode("admin1234"));
            System.out.println("user1234  = " + encoder.encode("user1234"));
        };
    }
}

