package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Institution Details ───────────────────────────────────────────────────
    @Column(nullable = false)
    private String institutionName;

    private String institutionType;     // GOVT / PRIVATE / NGO / COACHING

    private String board;               // CBSE / ICSE / State / IGCSE
    private Integer studentStrength;    // number of students
    private Integer classesFrom;        // e.g. 6
    private Integer classesTo;          // e.g. 12

    // ── Contact Person ────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String contactName;

    private String contactPhone;
    private String contactEmail;
    private String designation;         // Principal / Coordinator / Director

    // ── Location ─────────────────────────────────────────────────────────────
    private String city;
    private String district;
    private String state;
    private String pincode;
    private String address;

    // ── Services Requested ───────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String servicesRequired;    // comma-separated: AI,CAREER,SOFT_SKILLS,...

    private String preferredDuration;   // ONE_DAY / WEEKLY / MONTHLY / FULL_TERM
    private String preferredMode;       // ONLINE / OFFLINE / HYBRID
    private String budget;              // LOW / MEDIUM / HIGH / CUSTOM

    @Column(columnDefinition = "TEXT")
    private String additionalNote;

    // ── Admin fields ──────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private SchoolStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private String assignedMentors;     // comma-separated teacher IDs

    private String packageType;         // BASIC / STANDARD / PREMIUM / CUSTOM
    private String contractStartDate;
    private String contractEndDate;

    // ── Auth link ─────────────────────────────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                  // null for offline registrations

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp  private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = SchoolStatus.INQUIRY;
    }

    public enum SchoolStatus {
        INQUIRY,        // Just submitted the form
        CONTACTED,      // Admin has reached out
        PROPOSAL_SENT,  // Package proposal sent
        ONBOARDED,      // Contract signed, active
        ACTIVE,         // Sessions ongoing
        COMPLETED,      // Contract completed
        PAUSED          // Temporarily paused
    }
}