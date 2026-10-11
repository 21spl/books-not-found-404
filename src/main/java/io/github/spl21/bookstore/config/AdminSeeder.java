package io.github.spl21.bookstore;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.enums.Role;
import io.github.spl21.bookstore.repository.UserRepository;

@Configuration
public class AdminSeeder {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.existsByUsername(adminUsername)) {
                System.out.println("Admin already exists. Skipping seeding.");
                return;
            }

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(new HashSet<>(Set.of(Role.ADMIN)));

            userRepository.save(admin);

            System.out.println("Admin user seeded successfully.");
        };
    }
}