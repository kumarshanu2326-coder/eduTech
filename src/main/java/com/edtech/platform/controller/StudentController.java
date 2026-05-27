package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.StudentDto;
import com.edtech.platform.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    public StudentController(StudentService studentService) { this.studentService = studentService; }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto.Response>> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getByUserId(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentDto.Response>> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody StudentDto.ProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", studentService.updateProfile(userId, req)));
    }
}
