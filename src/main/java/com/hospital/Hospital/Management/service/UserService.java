package com.hospital.Hospital.Management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.Hospital.Management.dto.UserProfileUpdateRequest;
import com.hospital.Hospital.Management.exception.ProfileUpdateException;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

   
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

   
    @Transactional
    public User updateUserProfile(UserProfileUpdateRequest request) {
        User currentUser = getCurrentUser();
        logger.info("Updating profile for user: {}", currentUser.getEmail());

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            currentUser.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }

      
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
       
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                throw new BadCredentialsException("Current password is required to update email");
            }

            
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ProfileUpdateException("Email is already in use");
            }

            currentUser.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            
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