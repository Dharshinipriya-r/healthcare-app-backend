package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.AuthenticationRequest;
import com.hospital.Hospital.Management.dto.AuthenticationResponse;
import com.hospital.Hospital.Management.dto.RegisterRequest;
import com.hospital.Hospital.Management.dto.ResetPasswordRequest;
import com.hospital.Hospital.Management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Public endpoint for patient registration only
     * Role is hardcoded to PATIENT in the service layer
     * @param request The registration request containing email, password, and fullName
     * @return Response with success message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Patient registered successfully. Please check your email for verification."));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in with your new password."));
    }


//    @PostMapping("/forgot-password")
//    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
//        authService.requestPasswordReset(email);
//        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
//        authService.resetPassword(request);
//        return ResponseEntity.ok("Password has been reset successfully. You can now log in with your new password.");
//    }
}
