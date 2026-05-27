package com.edtech.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

public class StudentDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileRequest {
        private String  fullName;   // admin can update display name
        private String  school;
        private String  currentClass;
        private String  preferredSubjects;

        // Location
        private String area;
        private String country;
        private String state;
        private String district;
        private String city;
        private String pincode;

        // Deep profiling
        private String  fieldOfInterest;
        private String  weakSubjects;
        private String  careerAspiration;
        private String  techInterests;
        private String  learningHurdle;
        private String  preferredLearningStyle;
        private String  communicationLevel;
        private Boolean interestedInWorkshops;
        private Boolean interestedInInternships;
        private String  languagePreference;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private Long   userId;
        private String fullName;
        private String email;
        private String phone;
        private String school;
        private String currentClass;
        private String preferredSubjects;

        // Location
        private String area;
        private String country;
        private String state;
        private String district;
        private String city;
        private String pincode;

        // Deep profile
        private String  fieldOfInterest;
        private String  weakSubjects;
        private String  careerAspiration;
        private String  techInterests;
        private String  learningHurdle;
        private String  preferredLearningStyle;
        private String  communicationLevel;
        private Boolean interestedInWorkshops;
        private Boolean interestedInInternships;
        private String  languagePreference;

        private Boolean hasPaidPlatformFee;
        private LocalDateTime createdAt;
    }
}
