package com.hospital.Hospital.Management.dto;

import java.util.Set;

import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private Set<Role> roles;
    private boolean enabled;

    
    public static UserProfileResponse fromUser(User user) {
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