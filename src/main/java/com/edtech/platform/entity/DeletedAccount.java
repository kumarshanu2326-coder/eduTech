package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Audit table — every deleted/deactivated account is recorded here.
 * Original user row is anonymised but kept for referential integrity.
 */
@Entity
@Table(name = "deleted_accounts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeletedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long   originalUserId;
    private String fullName;          // snapshot at time of deletion
    private String email;
    private String phone;
    private String role;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;    // DEACTIVATED | DELETED

    private String deletedBy;         // "SELF" or admin email
    private String reason;            // user-provided or admin note

    private LocalDateTime actionAt;

    public enum ActionType { DEACTIVATED, DELETED }
}