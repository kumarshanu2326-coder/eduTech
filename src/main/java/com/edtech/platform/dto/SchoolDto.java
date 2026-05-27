package com.edtech.platform.dto;

import com.edtech.platform.entity.School;
import lombok.*;
import java.time.LocalDateTime;

public class SchoolDto {

    /** What the school submits on the registration form */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        private String  institutionName;
        private String  institutionType;
        private String  board;
        private Integer studentStrength;
        private Integer classesFrom;
        private Integer classesTo;
        // Contact
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String designation;
        // Location
        private String city;
        private String district;
        private String state;
        private String pincode;
        private String address;
        // Services
        private String servicesRequired;
        private String preferredDuration;
        private String preferredMode;
        private String budget;
        private String additionalNote;
    }

    /** Full response — used by admin + school dashboard */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private String institutionName;
        private String institutionType;
        private String board;
        private Integer studentStrength;
        private Integer classesFrom;
        private Integer classesTo;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String designation;
        private String city;
        private String district;
        private String state;
        private String address;
        private String servicesRequired;
        private String preferredDuration;
        private String preferredMode;
        private String budget;
        private String additionalNote;
        private String status;
        private String adminNotes;
        private String assignedMentors;
        private String packageType;
        private String contractStartDate;
        private String contractEndDate;
        private LocalDateTime createdAt;
    }

    /** Admin update request */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdminUpdate {
        private String status;
        private String adminNotes;
        private String assignedMentors;
        private String packageType;
        private String contractStartDate;
        private String contractEndDate;
    }
}