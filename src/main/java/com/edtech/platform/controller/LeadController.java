package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.LeadDto;
import com.edtech.platform.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;
    public LeadController(LeadService leadService) { this.leadService = leadService; }

    @PostMapping
    public ResponseEntity<ApiResponse<LeadDto.Response>> create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody LeadDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Lead created", leadService.create(userId, req)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LeadDto.Response>>> myLeads(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(leadService.getMyLeads(userId)));
    }

    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<List<LeadDto.Response>>> incoming(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(leadService.getIncoming(userId)));
    }

    @PatchMapping("/{leadId}/status")
    public ResponseEntity<ApiResponse<LeadDto.Response>> updateStatus(
            @PathVariable Long leadId,
            @RequestBody LeadDto.StatusUpdate req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", leadService.updateStatus(leadId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeadDto.Response>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(leadService.getAll()));
    }
}
