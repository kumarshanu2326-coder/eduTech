package com.edtech.platform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TeacherDto {

    // ── Profile update request ──────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileRequest {
        private String     fullName;   // admin can update display name
        private String     bio;
        private String     subjects;
        private String     subjectCategory;
        private String     classesHandled;
        private BigDecimal feesPerMonth;
        private String     timings;
        private String     mode;
        private String     languages;
        private String     qualifications;
        private Integer    experienceYears;
        private String     meetingLink;
        private String     availability;

        // Location
        private String location;     // legacy
        private String country;
        private String state;
        private String district;
        private String city;
        private String area;
        private String pincode;
    }

    // ── Full response ───────────────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long       id;
        private Long       userId;
        private String     fullName;
        private String     email;
        private String     phone;
        private String     bio;
        private String     subjects;
        private String     subjectCategory;
        private String     classesHandled;
        private BigDecimal feesPerMonth;
        private String     currency;
        private String     timings;
        private String     mode;
        private String     languages;
        private String     qualifications;
        private Integer    experienceYears;
        private String     meetingLink;
        private String     availability;

        // Location
        private String location;
        private String country;
        private String state;
        private String district;
        private String city;
        private String area;
        private String pincode;

        // Verification
        private Boolean hasDocuments;      // true if any doc uploaded
        private String  approvalStatus;
        private String  rejectionReason;

        // Flags
        private Boolean isSchoolRecommended;
        private Boolean isFeatured;
        private Boolean isSubscribed;
        private Double  rating;            // 0.0 – 5.0
        private Integer reviewCount;

        private LocalDateTime createdAt;
    }

    // ── Document upload response ────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DocumentStatus {
        private boolean marksheet10;
        private boolean marksheet12;
        private boolean graduation;
        private boolean certificates;
        private boolean experienceProof;
        private boolean resume;
        private String  approvalStatus;
    }
}
