package com.hospital.Hospital.Management.service;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hospital.Hospital.Management.dto.AdminUserCreationRequest;
import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.exception.UserAlreadyExistsException;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private AdminUserCreationRequest request;
    private User testUser;

    @BeforeEach
    void setUp() {
        request = new AdminUserCreationRequest();
        request.setEmail("doctor@example.com");
        request.setPassword("password123");
        request.setFullName("Dr. John Doe");
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Medical Street");

        testUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .roles(Collections.singleton(Role.ROLE_DOCTOR))
                .enabled(true)
                .build();
    }

    @Test
    void createDoctor_Success() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserProfileResponse response = adminService.createDoctor(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFullName(), response.getFullName());
        assertEquals(request.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(request.getAddress(), response.getAddress());
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createDoctor_UserAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            adminService.createDoctor(request);
        });

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createAdmin_Success() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        User adminUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .roles(Collections.singleton(Role.ROLE_ADMIN))
                .enabled(true)
                .build();
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // Act
        UserProfileResponse response = adminService.createAdmin(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFullName(), response.getFullName());
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }
}
