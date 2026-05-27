package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.TeacherDto;
import com.edtech.platform.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    public TeacherController(TeacherService teacherService) { this.teacherService = teacherService; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherDto.Response>>> search(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String classLevel,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok(
                teacherService.search(subject, classLevel, mode, city, state, district, category)));
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<TeacherDto.Response>>> recommended() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getRecommended()));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<TeacherDto.Response>>> featured() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getFeatured()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getByUserId(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TeacherDto.ProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", teacherService.updateProfile(userId, req)));
    }

    @PostMapping("/me/documents/{docType}")
    public ResponseEntity<ApiResponse<TeacherDto.DocumentStatus>> uploadDocument(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String docType,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Document uploaded",
                teacherService.uploadDocument(userId, docType, file)));
    }

    @GetMapping("/me/documents")
    public ResponseEntity<ApiResponse<TeacherDto.DocumentStatus>> getDocumentStatus(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getDocumentStatus(userId)));
    }

    /** Upload profile photo — max 1 MB */
    @PostMapping("/me/photo")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> uploadProfilePhoto(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {
        if (file.getSize() > 1024 * 1024)
            return ResponseEntity.badRequest().body(ApiResponse.error("Profile photo must be under 1 MB"));
        return ResponseEntity.ok(ApiResponse.ok("Photo uploaded",
                teacherService.uploadProfilePhoto(userId, file)));
    }
}
