package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "queries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;

    @Enumerated(EnumType.STRING)
    private QueryStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = QueryStatus.OPEN;
    }

    public enum QueryStatus { OPEN, IN_PROGRESS, RESOLVED }
}
