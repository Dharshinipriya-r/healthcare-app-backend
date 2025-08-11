package com.hospital.Hospital.Management.config;

import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

/**
 * Configuration class to initialize the first admin user on application startup
 * if no admin exists in the database.
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * Creates a CommandLineRunner bean that checks if any admin users exist in the database.
     * If no admin users exist, it creates a default admin user.
     *
     * @param userRepository Repository for user operations
     * @param passwordEncoder Password encoder for securing the admin password
     * @return CommandLineRunner that initializes the admin user
     */
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