package com.edtech.platform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ═══════════════════════════════════════════════════════════════════════════
//  LeadDto
// ═══════════════════════════════════════════════════════════════════════════
class LeadDtoHolder {}

public class LeadDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private Long   teacherId;
        private String studentNote;
        private String type;       // DEMO | ENROLLMENT
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatusUpdate {
        private String status;
        private String teacherResponse;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private Long   studentId;
        private String studentName;
        private String studentPhone;
        private Long   teacherId;
        private String teacherName;
        private String status;
        private String type;
        private String studentNote;
        private String teacherResponse;
        private Boolean isPaid;
        private LocalDateTime demoScheduledAt;
        private LocalDateTime createdAt;
    }
}
