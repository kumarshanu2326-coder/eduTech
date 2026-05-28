package com.edtech.platform.service;

import com.edtech.platform.entity.DeletedAccount;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.DeletedAccountRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountService {

    private final UserRepository          userRepo;
    private final DeletedAccountRepository deletedRepo;

    public AccountService(UserRepository userRepo, DeletedAccountRepository deletedRepo) {
        this.userRepo    = userRepo;
        this.deletedRepo = deletedRepo;
    }

    /**
     * DEACTIVATE — hides profile from all listings.
     * Account still exists; user can reactivate by contacting support.
     * Logged to deleted_accounts for audit.
     */
    @Transactional
    public void deactivateAccount(Long userId, String reason, String actorEmail) {
        User user = getUser(userId);
        user.setIsActive(false);
        userRepo.save(user);
        audit(user, DeletedAccount.ActionType.DEACTIVATED, actorEmail, reason);
    }

    /**
     * REACTIVATE — admin can restore a deactivated account.
     */
    @Transactional
    public void reactivateAccount(Long userId, String adminEmail) {
        User user = getUser(userId);
        user.setIsActive(true);
        userRepo.save(user);
    }

    /**
     * SOFT DELETE — anonymises PII in users table, logs full snapshot to deleted_accounts.
     * Foreign keys remain valid; referential integrity preserved.
     * The account cannot be recovered after this.
     */
    @Transactional
    public void deleteAccount(Long userId, String reason, String actorEmail) {
        User user = getUser(userId);

        // 1. Snapshot into audit table BEFORE anonymising
        audit(user, DeletedAccount.ActionType.DELETED, actorEmail, reason);

        // 2. Anonymise PII — do NOT physically delete the row
        String anon = "deleted_" + userId;
        user.setFullName("Deleted User");
        user.setEmail(anon + "@deleted.educonnect.in");
        user.setPhone(null);
        user.setPassword("[DELETED]");
        user.setIsActive(false);
        userRepo.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void audit(User user, DeletedAccount.ActionType type, String deletedBy, String reason) {
        deletedRepo.save(DeletedAccount.builder()
                .originalUserId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : "UNKNOWN")
                .actionType(type)
                .deletedBy(deletedBy != null ? deletedBy : "SELF")
                .reason(reason)
                .actionAt(LocalDateTime.now())
                .build());
    }

    private User getUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}