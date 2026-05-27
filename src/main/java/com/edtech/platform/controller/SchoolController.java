package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.SchoolDto;
import com.edtech.platform.service.SchoolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    // ── Public ────────────────────────────────────────────────────────────────

    /** Any school can register — no auth required */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SchoolDto.Response>> register(
            @RequestBody SchoolDto.RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Registration submitted successfully! Our team will contact you within 24 hours.",
                schoolService.register(req)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDto.Response>>> getAll(
            @RequestParam(required = false) String status) {
        List<SchoolDto.Response> list = status != null && !status.isBlank()
                ? schoolService.getByStatus(status)
                : schoolService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDto.Response>> adminUpdate(
            @PathVariable Long id, @RequestBody SchoolDto.AdminUpdate req) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", schoolService.adminUpdate(id, req)));
    }
}