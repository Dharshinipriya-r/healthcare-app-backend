package com.hospital.Hospital.Management.config;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;


@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if any admin users exist
            boolean adminExists = userRepository.findAll().stream()
                    .flatMap(user -> user.getRoles().stream())
                    .anyMatch(role -> role == Role.ROLE_ADMIN);

            if (!adminExists) {
                logger.info("No admin users found. Creating default admin user...");

                // Create default admin user
                User adminUser = User.builder()
                        .email("admin@hospital.com")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Administrator")
                        .roles(Collections.singleton(Role.ROLE_ADMIN))
                        .enabled(true)
                        .build();

                userRepository.save(adminUser);
                logger.info("Default admin user created successfully with email: {}", adminUser.getEmail());
            } else {
                logger.info("Admin user(s) already exist. Skipping default admin creation.");
            }
        };
    }
}