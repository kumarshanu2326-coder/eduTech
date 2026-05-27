package com.edtech.platform.dto;

import lombok.*;

public class FounderDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private String name;
        private String title;
        private String tagline;
        private String story;
        private String photoPath;
        private String linkedinUrl;
        private String city;
    }
}