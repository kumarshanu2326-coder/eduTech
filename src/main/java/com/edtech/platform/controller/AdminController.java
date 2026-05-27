package com.edtech.platform.controller;

import com.edtech.platform.dto.*;
import com.edtech.platform.entity.Payment;
import com.edtech.platform.repository.PaymentRepository;
import com.edtech.platform.repository.UserRepository;
import com.edtech.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService      adminService;
    private final TeacherService    teacherService;
    private final StudentService    studentService;
    private final TestService       testService;
    private final PaymentRepository paymentRepo;
    private final UserRepository    userRepo;

    public AdminController(AdminService adminService, TeacherService teacherService,
                           StudentService studentService, TestService testService,
                           PaymentRepository paymentRepo, UserRepository userRepo) {
        this.adminService   = adminService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.testService    = testService;
        this.paymentRepo    = paymentRepo;
        this.userRepo       = userRepo;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminService.DashboardStats>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // ── Teachers ───────────────────────────────────────────────────────────

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<TeacherDto.Response>>> allTeachers(
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(ApiResponse.ok(
                "ALL".equalsIgnoreCase(status) ? teacherService.getAll()
                        : adminService.getTeachersByStatus(status)));
    }

    @GetMapping("/teachers/pending")
    public ResponseEntity<ApiResponse<List<TeacherDto.Response>>> pendingTeachers() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getPending()));
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getById(id)));
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> editTeacher(
            @PathVariable Long id, @RequestBody TeacherDto.ProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Teacher profile updated",
                adminService.editTeacherProfile(id, req)));
    }

    @PatchMapping("/teachers/{id}/approve")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Teacher approved", teacherService.approve(id)));
    }

    @PatchMapping("/teachers/{id}/reject")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok("Teacher rejected", teacherService.reject(id, reason)));
    }

    @PatchMapping("/teachers/{id}/suspend")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Teacher suspended", teacherService.suspend(id)));
    }

    @PatchMapping("/teachers/{id}/recommend")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> toggleRecommend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Recommendation toggled",
                teacherService.toggleRecommended(id)));
    }

    @PatchMapping("/teachers/{id}/featured")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Featured toggled", teacherService.toggleFeatured(id)));
    }

    @PatchMapping("/teachers/{id}/subscription")
    public ResponseEntity<ApiResponse<TeacherDto.Response>> activateSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Subscription activated",
                teacherService.activateSubscription(id)));
    }

    // ── Students ───────────────────────────────────────────────────────────

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentDto.Response>>> allStudents() {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAll()));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentDto.Response>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getById(id)));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentDto.Response>> editStudent(
            @PathVariable Long id, @RequestBody StudentDto.ProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Student profile updated",
                adminService.editStudentProfile(id, req)));
    }

    // ── Tests ──────────────────────────────────────────────────────────────

    @GetMapping("/tests")
    public ResponseEntity<ApiResponse<List<TestDto.TestResponse>>> allTests() {
        return ResponseEntity.ok(ApiResponse.ok(testService.getActiveTests()));
    }

    @PostMapping("/tests")
    public ResponseEntity<ApiResponse<TestDto.TestResponse>> createTest(
            @RequestBody TestDto.CreateTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test created", testService.createTest(req)));
    }

    @PutMapping("/tests/{id}")
    public ResponseEntity<ApiResponse<TestDto.TestResponse>> updateTest(
            @PathVariable Long id, @RequestBody TestDto.CreateTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test updated", testService.updateTest(id, req)));
    }

    // ── Founder photo upload ───────────────────────────────────────────────

    @PostMapping("/founder/photo")
    public ResponseEntity<ApiResponse<String>> uploadFounderPhoto(
            @RequestParam("file") MultipartFile file) {
        if (file.getSize() > 3 * 1024 * 1024)
            return ResponseEntity.badRequest().body(ApiResponse.error("Max file size 3MB"));
        try {
            Path dir = Paths.get("uploads/founder/");
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(ApiResponse.ok("Photo uploaded", "uploads/founder/" + name));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Upload failed"));
        }
    }

    // ── Revenue & Ledger ───────────────────────────────────────────────────

    /** All transactions ordered newest first */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> allTransactions() {
        List<Map<String, Object>> result = paymentRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",              p.getId());
                    m.put("userName",        p.getUserName() != null ? p.getUserName()
                            : (p.getUser() != null ? p.getUser().getFullName() : "Unknown"));
                    m.put("userType",        p.getUserType() != null ? p.getUserType() : "STUDENT");
                    m.put("amount",          p.getAmount());
                    m.put("paymentMode",     p.getPaymentMode() != null ? p.getPaymentMode() : "ONLINE");
                    m.put("purpose",         p.getPurpose() != null ? p.getPurpose()
                            : (p.getType() != null ? p.getType().name() : "OTHER"));
                    m.put("status",          p.getStatus() != null ? p.getStatus().name() : "SUCCESS");
                    m.put("transactionId",   p.getRazorpayPaymentId());
                    m.put("referenceNumber", p.getReferenceNumber());
                    m.put("note",            p.getNote());
                    m.put("createdAt",       p.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** Admin records a manual / cash / offline transaction */
    @PostMapping("/transactions/manual")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addManualTransaction(
            @RequestBody Map<String, Object> req) {

        // Revenue always needs a user FK — use the first admin account found
        var adminUser = userRepo.findAll().stream()
                .filter(u -> u.getRole() != null && "ADMIN".equals(u.getRole().name()))
                .findFirst()
                .orElseGet(() -> userRepo.findAll().get(0));

        String purposeStr = req.getOrDefault("purpose", "MANUAL_CASH").toString();
        Payment.PaymentType type;
        try   { type = Payment.PaymentType.valueOf(purposeStr); }
        catch (Exception e) { type = Payment.PaymentType.MANUAL_CASH; }

        Payment p = Payment.builder()
                .user(adminUser)
                .amount(new BigDecimal(req.getOrDefault("amount", "0").toString()))
                .status(Payment.PaymentStatus.SUCCESS)
                .type(type)
                .userName(req.getOrDefault("userName", "").toString())
                .userType(req.getOrDefault("userType", "OTHER").toString())
                .paymentMode(req.getOrDefault("paymentMode", "CASH").toString())
                .purpose(purposeStr)
                .note(req.getOrDefault("note", "").toString())
                .referenceNumber(req.getOrDefault("referenceNumber", "").toString())
                .build();

        paymentRepo.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Transaction recorded",
                Map.of("id", p.getId(), "amount", p.getAmount())));
    }
}