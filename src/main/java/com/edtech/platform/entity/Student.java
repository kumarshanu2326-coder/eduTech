package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // ── Academic profile ────────────────────────────────────────────────────
    private String school;
    private String currentClass;
    private String preferredSubjects;    // comma-separated
    private Boolean hasPaidPlatformFee;

    // ── Location (granular) ─────────────────────────────────────────────────
    private String area;        // legacy
    private String country;
    private String state;
    private String district;
    private String city;
    private String pincode;

    // ── Deep profiling for recommendation engine ────────────────────────────
    private String fieldOfInterest;         // e.g. "Engineering", "Medicine", "Arts"
    private String weakSubjects;            // comma-separated
    private String careerAspiration;        // free text
    private String techInterests;           // "AI,Python,Web Dev"
    private String learningHurdle;          // e.g. "Lack of good teachers nearby"
    private String preferredLearningStyle;  // "Visual", "Practice-based", "Lecture"
    private String communicationLevel;      // "Beginner", "Intermediate", "Advanced"
    private Boolean interestedInWorkshops;
    private Boolean interestedInInternships;
    private String languagePreference;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (hasPaidPlatformFee      == null) hasPaidPlatformFee      = false;
        if (interestedInWorkshops   == null) interestedInWorkshops   = false;
        if (interestedInInternships == null) interestedInInternships = false;
        if (country                 == null) country                 = "India";
    }
}
