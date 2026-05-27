package com.edtech.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

// ── Banner ─────────────────────────────────────────────────────────────────

public class BannerDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private String title;
        private String subtitle;
        private String ctaText;
        private String ctaLink;
        private String backgroundColor;
        private String badgeText;
        private String type;
        private Boolean isActive;
        private Integer displayOrder;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long   id;
        private String title;
        private String subtitle;
        private String ctaText;
        private String ctaLink;
        private String imagePath;
        private String backgroundColor;
        private String badgeText;
        private String type;
        private Boolean isActive;
        private Integer displayOrder;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private LocalDateTime createdAt;
    }
}
