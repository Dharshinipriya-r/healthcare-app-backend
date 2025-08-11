package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.UserProfileUpdateRequest;
import com.hospital.Hospital.Management.exception.InvalidTokenException;
import com.hospital.Hospital.Management.exception.ProfileUpdateException;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the currently authenticated user
     * @return The authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Update the user profile
     * @param request The profile update request
     * @return The updated user profile
     */
    @Transactional
    public User updateUserProfile(UserProfileUpdateRequest request) {
        User currentUser = getCurrentUser();
        logger.info("Updating profile for user: {}", currentUser.getEmail());

        // Update basic profile information
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            currentUser.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }

        // Handle email update (requires password verification)
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            // Check if current password is provided and correct
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                throw new BadCredentialsException("Current password is required to update email");
            }

            // Check if email is already in use
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ProfileUpdateException("Email is already in use");
            }

            currentUser.setEmail(request.getEmail());
        }

        // Handle password update
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // Verify current password
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                throw new BadCredentialsException("Current password is incorrect");
            }

            currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User updatedUser = userRepository.save(currentUser);
        logger.info("Profile updated successfully for user: {}", updatedUser.getEmail());

        return updatedUser;
    }
}