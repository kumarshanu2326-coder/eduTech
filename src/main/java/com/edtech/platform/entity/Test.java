package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // "AI Foundations Assessment"
    private String subject;        // "Artificial Intelligence"
    private String category;       // "Future Skills"
    private String description;
    private String difficulty;     // BEGINNER / INTERMEDIATE / ADVANCED
    private String mode;           // ONLINE / OFFLINE

    private Boolean isFree;        // Free foundation tests
    private BigDecimal price;      // For paid tests

    private Integer totalQuestions;
    private Integer durationMinutes;
    private Integer passingScore;  // percentage 0-100

    private Boolean isActive;
    private Integer displayOrder;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (isFree    == null) isFree    = true;
        if (isActive  == null) isActive  = true;
        if (price     == null) price     = BigDecimal.ZERO;
        if (difficulty== null) difficulty= "BEGINNER";
        if (mode      == null) mode      = "ONLINE";
        if (totalQuestions   == null) totalQuestions   = 20;
        if (durationMinutes  == null) durationMinutes  = 30;
        if (passingScore     == null) passingScore     = 60;
        if (displayOrder     == null) displayOrder     = 0;
    }
}
