package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.dto.UserProfileUpdateRequest;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get the current user's profile
     * @return The user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(UserProfileResponse.fromUser(currentUser));
    }

    /**
     * Update the current user's profile
     * @param request The profile update request
     * @return The updated user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        User updatedUser = userService.updateUserProfile(request);
        return ResponseEntity.ok(UserProfileResponse.fromUser(updatedUser));
    }
}