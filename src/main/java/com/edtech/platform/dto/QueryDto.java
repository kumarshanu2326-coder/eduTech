package com.edtech.platform.dto;

import lombok.*;
import java.time.LocalDateTime;

public class QueryDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private String name;
        private String email;
        private String phone;
        private String subject;
        private String message;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private String name;
        private String email;
        private String phone;
        private String subject;
        private String message;
        private String status;
        private LocalDateTime createdAt;
    }
}
