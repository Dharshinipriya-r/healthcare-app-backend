package com.hospital.Hospital.Management.dto;

import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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

    /**
     * Factory method to create a UserProfileResponse from a User entity
     * @param user The user entity
     * @return A UserProfileResponse object
     */
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