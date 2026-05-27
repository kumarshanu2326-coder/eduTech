package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_answers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String givenAnswer;

    private Boolean isCorrect;
    private Boolean isMarkedForReview;
    private Boolean isSkipped;
    private Integer marksAwarded;

    @PrePersist
    public void prePersist() {
        if (isCorrect         == null) isCorrect         = false;
        if (isMarkedForReview == null) isMarkedForReview = false;
        if (isSkipped         == null) isSkipped         = false;
        if (marksAwarded      == null) marksAwarded      = 0;
    }
}