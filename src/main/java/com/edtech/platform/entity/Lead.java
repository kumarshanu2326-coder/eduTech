package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    private LeadType type;

    private String        studentNote;
    private Boolean       isPaid;
    private String        paymentId;
    private String        paymentOrderId;
    private LocalDateTime demoScheduledAt;
    private String        teacherResponse;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = LeadStatus.NEW;
        if (type   == null) type   = LeadType.DEMO;
        if (isPaid == null) isPaid = false;
    }

    public enum LeadStatus { NEW, ACCEPTED, REJECTED, COMPLETED, CANCELLED }
    public enum LeadType   { DEMO, ENROLLMENT }
}
