package com.edtech.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateTestRequest {
        private String     title;
        private String     subject;
        private String     category;
        private String     description;
        private String     difficulty;
        private String     mode;
        private Boolean    isFree;
        private BigDecimal price;
        private Integer    totalQuestions;
        private Integer    durationMinutes;
        private Integer    passingScore;
        private Integer    displayOrder;
        private Boolean    isActive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TestResponse {
        private Long       id;
        private String     title;
        private String     subject;
        private String     category;
        private String     description;
        private String     difficulty;
        private String     mode;
        private Boolean    isFree;
        private BigDecimal price;
        private Integer    totalQuestions;
        private Integer    durationMinutes;
        private Integer    passingScore;
        private Boolean    isActive;
        private Integer    displayOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubmitAttemptRequest {
        private Long    testId;
        private Integer correctAnswers;
        private Integer timeTakenSeconds;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AttemptResponse {
        private Long          id;
        private Long          testId;
        private String        testTitle;
        private String        subject;
        private Integer       score;
        private Integer       correctAnswers;
        private Integer       totalQuestions;
        private Boolean       passed;
        private String        feedback;
        private LocalDateTime attemptedAt;
    }

    /** Per-subject skill card for student dashboard */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SkillProgress {
        private String  subject;
        private Integer bestScore;
        private String  level;           // Needs Attention / Beginner / Intermediate / Advanced
        private Integer progressPercent;
        private Integer attemptsCount;
    }
}
