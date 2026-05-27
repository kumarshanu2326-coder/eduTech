package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.TestDto;
import com.edtech.platform.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;
    public TestController(TestService testService) { this.testService = testService; }

    /** Public: all active tests */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TestDto.TestResponse>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(testService.getActiveTests()));
    }

    /** Public: free tests only */
    @GetMapping("/free")
    public ResponseEntity<ApiResponse<List<TestDto.TestResponse>>> free() {
        return ResponseEntity.ok(ApiResponse.ok(testService.getFreeTests()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestDto.TestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(testService.getTestById(id)));
    }

    /** Student: submit attempt */
    @PostMapping("/attempt")
    public ResponseEntity<ApiResponse<TestDto.AttemptResponse>> submit(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TestDto.SubmitAttemptRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Result saved", testService.submitAttempt(userId, req)));
    }

    /** Student: my skill progress */
    @GetMapping("/my-progress")
    public ResponseEntity<ApiResponse<List<TestDto.SkillProgress>>> myProgress(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(testService.getSkillProgress(userId)));
    }

    /** Student: my attempts history */
    @GetMapping("/my-attempts")
    public ResponseEntity<ApiResponse<List<TestDto.AttemptResponse>>> myAttempts(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(testService.getMyAttempts(userId)));
    }

    // Admin CRUD
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<TestDto.TestResponse>> create(@RequestBody TestDto.CreateTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test created", testService.createTest(req)));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<TestDto.TestResponse>> update(
            @PathVariable Long id, @RequestBody TestDto.CreateTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test updated", testService.updateTest(id, req)));
    }
}
