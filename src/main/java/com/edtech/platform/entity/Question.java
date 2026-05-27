package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    private String imageUrl;

    // JSON array string: ["Option A","Option B","Option C","Option D"]
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    // MCQ: "0","1","2","3" | Multiple: "0,2" | T/F: "true"/"false" | Descriptive: model answer
    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private Integer marks;
    private Integer negativeMarks;
    private Integer displayOrder;
    private Boolean isMandatory;

    @PrePersist
    public void prePersist() {
        if (marks         == null) marks         = 1;
        if (negativeMarks == null) negativeMarks = 0;
        if (displayOrder  == null) displayOrder  = 0;
        if (isMandatory   == null) isMandatory   = false;
    }

    public enum QuestionType {
        MCQ,
        MULTIPLE_CORRECT,
        TRUE_FALSE,
        FILL_IN_BLANK,
        DESCRIPTIVE,
        LONG_ANSWER
    }
}