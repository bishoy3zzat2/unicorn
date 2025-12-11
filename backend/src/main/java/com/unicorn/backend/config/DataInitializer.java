package com.unicorn.backend.config;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@gmail.com";

            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                admin.setAuthProvider("LOCAL");

                userRepository.save(admin);
                System.out.println("Default Admin User created: " + adminEmail + " / admin");
            }
        };
    }
}
