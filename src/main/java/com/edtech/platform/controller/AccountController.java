package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.entity.DeletedAccount;
import com.edtech.platform.repository.DeletedAccountRepository;
import com.edtech.platform.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService          accountService;
    private final DeletedAccountRepository deletedRepo;

    public AccountController(AccountService accountService,
                             DeletedAccountRepository deletedRepo) {
        this.accountService = accountService;
        this.deletedRepo    = deletedRepo;
    }

    // ── Self-service (user's own account) ─────────────────────────────────────

    /** User deactivates their own account */
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "User requested deactivation") : "User requested deactivation";
        accountService.deactivateAccount(userId, reason, "SELF");
        return ResponseEntity.ok(ApiResponse.ok("Account deactivated. Contact support to reactivate.", null));
    }

    /** User permanently deletes their own account */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "User requested deletion") : "User requested deletion";
        accountService.deleteAccount(userId, reason, "SELF");
        return ResponseEntity.ok(ApiResponse.ok("Account permanently deleted. Your data has been anonymised.", null));
    }

    // ── Admin controls ────────────────────────────────────────────────────────

    @PatchMapping("/admin/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> adminDeactivate(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Admin action") : "Admin action";
        accountService.deactivateAccount(userId, reason, "admin@educonnect.in");
        return ResponseEntity.ok(ApiResponse.ok("Account deactivated", null));
    }

    @PatchMapping("/admin/{userId}/reactivate")
    public ResponseEntity<ApiResponse<Void>> adminReactivate(@PathVariable Long userId) {
        accountService.reactivateAccount(userId, "admin@educonnect.in");
        return ResponseEntity.ok(ApiResponse.ok("Account reactivated", null));
    }

    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Admin deletion") : "Admin deletion";
        accountService.deleteAccount(userId, reason, "admin@educonnect.in");
        return ResponseEntity.ok(ApiResponse.ok("Account deleted and anonymised", null));
    }

    /** Admin — full audit log of all deleted/deactivated accounts */
    @GetMapping("/admin/audit")
    public ResponseEntity<ApiResponse<List<DeletedAccount>>> auditLog() {
        return ResponseEntity.ok(ApiResponse.ok(deletedRepo.findAllByOrderByActionAtDesc()));
    }
}