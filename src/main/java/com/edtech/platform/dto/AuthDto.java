package com.edtech.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        private String email;
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String role;        // STUDENT | TEACHER
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email or phone is required")
        private String identifier;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private Long   userId;
        private String fullName;
        private String email;
        private String phone;
        private String role;
        private Long   profileId;
        private String token;      // JWT placeholder — will be populated once security is enabled
    }
}
