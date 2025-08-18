package com.hospital.Hospital.Management.service;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.Hospital.Management.dto.AdminUserCreationRequest;
import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.exception.UserAlreadyExistsException;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResponse createDoctor(AdminUserCreationRequest request) {
        logger.info("Admin creating doctor with email: {}", request.getEmail());

       
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Doctor creation failed: user with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User doctor = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .roles(Collections.singleton(Role.ROLE_DOCTOR))
                .enabled(true) 
                .build();

        User savedDoctor = userRepository.save(doctor);
        logger.info("Doctor created successfully with ID: {}", savedDoctor.getId());

        return mapToUserProfileResponse(savedDoctor);
    }

   
    @Transactional
    public UserProfileResponse createAdmin(AdminUserCreationRequest request) {
        logger.info("Admin creating another admin with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Admin creation failed: user with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

     
        User admin = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .roles(Collections.singleton(Role.ROLE_ADMIN))
                .enabled(true)
                .build();

        User savedAdmin = userRepository.save(admin);
        logger.info("Admin created successfully with ID: {}", savedAdmin.getId());

        return mapToUserProfileResponse(savedAdmin);
    }

   
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .build();
    }
}