package com.hospital.Hospital.Management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.dto.UserProfileUpdateRequest;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(UserProfileResponse.fromUser(currentUser));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        User updatedUser = userService.updateUserProfile(request);
        return ResponseEntity.ok(UserProfileResponse.fromUser(updatedUser));
    }
}