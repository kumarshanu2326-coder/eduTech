package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "founder_profile")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FounderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String title;           // "Founder & CEO"
    private String tagline;         // Short punchy quote
    private String story;           // Full story/bio text
    private String photoPath;       // uploaded image path
    private String linkedinUrl;
    private String city;
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (isActive == null) isActive = true;
    }
}
