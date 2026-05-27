package com.edtech.platform.dto;

import com.edtech.platform.entity.Question;
import lombok.*;
import java.util.List;

public class QuestionDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private Question.QuestionType type;
        private String  questionText;
        private String  imageUrl;
        private List<String> options;
        private String  correctAnswer;
        private String  explanation;
        private Integer marks;
        private Integer negativeMarks;
        private Integer displayOrder;
        private Boolean isMandatory;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long    id;
        private Long    testId;
        private Question.QuestionType type;
        private String  questionText;
        private String  imageUrl;
        private List<String> options;
        private String  correctAnswer;
        private String  explanation;
        private Integer marks;
        private Integer negativeMarks;
        private Integer displayOrder;
        private Boolean isMandatory;
    }

    /** Used for CSV/JSON bulk upload preview */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BulkRow {
        private String  type;
        private String  questionText;
        private String  optionA;
        private String  optionB;
        private String  optionC;
        private String  optionD;
        private String  correctAnswer;
        private String  explanation;
        private Integer marks;
        private Integer negativeMarks;
        // validation
        private Boolean valid;
        private String  errorMessage;
    }
}