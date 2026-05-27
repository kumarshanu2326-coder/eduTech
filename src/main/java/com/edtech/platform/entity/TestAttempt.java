package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_attempts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test;

    private Integer score;           // 0-100 percentage
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer timeTakenSeconds;
    private Boolean passed;
    private String  feedback;        // auto-generated feedback text

    @Enumerated(EnumType.STRING)
    private AttemptStatus status;    // IN_PROGRESS, COMPLETED

    @CreationTimestamp
    private LocalDateTime attemptedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (status  == null) status  = AttemptStatus.IN_PROGRESS;
        if (passed  == null) passed  = false;
    }

    public enum AttemptStatus { IN_PROGRESS, COMPLETED }
}
