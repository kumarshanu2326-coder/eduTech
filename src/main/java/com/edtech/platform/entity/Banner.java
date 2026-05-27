package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;
    private String ctaText;
    private String ctaLink;
    private String imagePath;
    private String backgroundColor;    // hex or gradient string
    private String badgeText;          // e.g. "🔥 New", "📅 This Weekend"

    @Enumerated(EnumType.STRING)
    private BannerType type;           // WORKSHOP, WEBINAR, MENTOR, INTERNSHIP, etc.

    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (isActive     == null) isActive     = true;
        if (displayOrder == null) displayOrder = 0;
    }

    public enum BannerType {
        WORKSHOP, WEBINAR, MENTOR, INTERNSHIP, CAREER_EVENT,
        AI_BOOTCAMP, SUCCESS_STORY, SCHOLARSHIP, GENERAL
    }
}
