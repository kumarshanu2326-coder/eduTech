package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Channel channel;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    private String errorMessage;
    private String referenceId;
    private String referenceType;

    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (status  == null) status  = Status.PENDING;
        if (channel == null) channel = Channel.EMAIL;
    }

    public enum NotificationType {
        DEMO_BOOKED, DEMO_APPROVED, DEMO_REMINDER,
        LEAD_ACCEPTED, LEAD_REJECTED,
        TEST_COMPLETED, TEST_REMINDER,
        PAYMENT_SUCCESS, PAYMENT_FAILED,
        TEACHER_APPROVED, TEACHER_REJECTED,
        WELCOME_STUDENT, WELCOME_TEACHER,
        GENERAL
    }

    public enum Channel { EMAIL, SMS, IN_APP }
    public enum Status  { PENDING, SENT, FAILED }
}