package com.hospital.Hospital.Management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;

    // For frontend compatibility (expects `token` and `user.id`)
    private String token; // alias of accessToken
    private UserSummary user; // lightweight user payload for clients

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {
        private Long id;
        private String email;
        private String role; // single primary role for convenience
    }
}
