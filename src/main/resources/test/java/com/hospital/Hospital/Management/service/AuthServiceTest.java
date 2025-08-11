package com.hospital.Hospital.Management.service;

// --- Imports from your own application packages ---
import com.hospital.Hospital.Management.dto.RegisterRequest;
import com.hospital.Hospital.Management.exception.UserAlreadyExistsException;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.repository.VerificationTokenRepository;

// --- Imports from JUnit and Mockito Libraries ---
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

// --- STATIC IMPORTS (THIS IS THE FIX) ---
// This allows you to use methods like assertEquals() and assertThrows() directly.
import static org.junit.jupiter.api.Assertions.assertThrows;
// This allows you to use methods like when(), verify(), times(), never() directly.
import static org.mockito.Mockito.*;
// This allows you to use argument matchers like any() and anyString() directly.
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should successfully register a new user")
    void register_Success_WhenUserDoesNotExist() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser@test.com", "password123", "New User", "PATIENT");
        when(userRepository.existsByEmail(anyString())).thenReturn(false); // No error on when()
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Return the user object that would have been saved, complete with its email.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L); // Simulate the database assigning an ID
            return userToSave;
        });

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(any(User.class)); // No error on verify() or any()
        verify(tokenRepository, times(1)).save(any());
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when registering an existing user")
    void register_Failure_WhenUserAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existing@test.com", "password123", "Existing User", "DOCTOR");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> { // No error on assertThrows()
            authService.register(request);
        });

        verify(userRepository, never()).save(any()); // No error on never()
    }
}