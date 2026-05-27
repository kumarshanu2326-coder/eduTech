package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // ── Profile ────────────────────────────────────────────────────────────
    private String     bio;
    private String     subjects;           // comma-separated e.g. "Mathematics,AI,Python"
    private String     subjectCategory;    // e.g. "Technology & Coding"
    private String     classesHandled;     // "Grade 9,Grade 10,Grade 11"
    private BigDecimal feesPerMonth;
    private String     currency;
    private String     timings;
    private String     languages;          // "Hindi,English"
    private String     qualifications;
    private Integer    experienceYears;
    private String     meetingLink;
    private String     availability;

    @Enumerated(EnumType.STRING)
    private TeachingMode mode;

    // ── Location (granular) ────────────────────────────────────────────────
    private String location;      // legacy single-field (kept for backward compat)
    private String country;
    private String state;
    private String district;
    private String city;
    private String area;
    private String pincode;

    // ── Verification documents ─────────────────────────────────────────────
    private String marksheet10Path;
    private String marksheet12Path;
    private String graduationMarksheetPath;
    private String certificatesPath;         // comma-separated paths
    private String experienceProofPath;
    private String resumePath;

    // ── Status & flags ─────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private String  rejectionReason;
    private Boolean isSchoolRecommended;
    private Boolean isFeatured;
    private Boolean isSubscribed;
    private LocalDateTime subscriptionExpiry;
    private String  profilePhotoPath;
    private Integer rating;           // stored as 0–50 (divide by 10 for display)
    private Integer reviewCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (mode                == null) mode                = TeachingMode.BOTH;
        if (isSchoolRecommended == null) isSchoolRecommended = false;
        if (isFeatured          == null) isFeatured          = false;
        if (approvalStatus      == null) approvalStatus      = ApprovalStatus.PENDING;
        if (isSubscribed        == null) isSubscribed        = false;
        if (currency            == null) currency            = "INR";
        if (country             == null) country             = "India";
        if (rating              == null) rating              = 0;
        if (reviewCount         == null) reviewCount         = 0;
    }

    public enum TeachingMode   { ONLINE, OFFLINE, BOTH }
    public enum ApprovalStatus { PENDING, APPROVED, REJECTED, SUSPENDED }
}
