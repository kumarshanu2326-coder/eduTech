package com.edtech.platform.service;

import com.edtech.platform.dto.TestDto;
import com.edtech.platform.entity.*;
import com.edtech.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestRepository        testRepo;
    private final TestAttemptRepository attemptRepo;
    private final StudentRepository     studentRepo;
    private final UserRepository        userRepo;

    public TestService(TestRepository testRepo, TestAttemptRepository attemptRepo,
                       StudentRepository studentRepo, UserRepository userRepo) {
        this.testRepo    = testRepo;
        this.attemptRepo = attemptRepo;
        this.studentRepo = studentRepo;
        this.userRepo    = userRepo;
    }

    // ── Tests ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TestDto.TestResponse> getActiveTests() {
        return testRepo.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toTestResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestDto.TestResponse> getFreeTests() {
        return testRepo.findByIsFreeTrue()
                .stream().map(this::toTestResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestDto.TestResponse getTestById(Long id) {
        Test t = testRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));
        return toTestResponse(t);
    }

    @Transactional
    public TestDto.TestResponse createTest(TestDto.CreateTestRequest req) {
        Test t = Test.builder()
                .title(req.getTitle())
                .subject(req.getSubject())
                .category(req.getCategory())
                .description(req.getDescription())
                .difficulty(req.getDifficulty())
                .mode(req.getMode())
                .isFree(req.getIsFree() != null ? req.getIsFree() : true)
                .price(req.getPrice() != null ? req.getPrice() : BigDecimal.ZERO)
                .totalQuestions(req.getTotalQuestions())
                .durationMinutes(req.getDurationMinutes())
                .passingScore(req.getPassingScore())
                .displayOrder(req.getDisplayOrder())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();
        return toTestResponse(testRepo.save(t));
    }

    @Transactional
    public TestDto.TestResponse updateTest(Long id, TestDto.CreateTestRequest req) {
        Test t = testRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Test not found"));
        if (req.getTitle()          != null) t.setTitle(req.getTitle());
        if (req.getSubject()        != null) t.setSubject(req.getSubject());
        if (req.getDescription()    != null) t.setDescription(req.getDescription());
        if (req.getDifficulty()     != null) t.setDifficulty(req.getDifficulty());
        if (req.getIsFree()         != null) t.setIsFree(req.getIsFree());
        if (req.getPrice()          != null) t.setPrice(req.getPrice());
        if (req.getTotalQuestions() != null) t.setTotalQuestions(req.getTotalQuestions());
        if (req.getDurationMinutes()!= null) t.setDurationMinutes(req.getDurationMinutes());
        if (req.getPassingScore()   != null) t.setPassingScore(req.getPassingScore());
        if (req.getIsActive()       != null) t.setIsActive(req.getIsActive());
        if (req.getDisplayOrder()   != null) t.setDisplayOrder(req.getDisplayOrder());
        return toTestResponse(testRepo.save(t));
    }

    // ── Attempts ───────────────────────────────────────────────

    @Transactional
    public TestDto.AttemptResponse submitAttempt(Long userId, TestDto.SubmitAttemptRequest req) {
        User    user    = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Student student = studentRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        Test    test    = testRepo.findById(req.getTestId())
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));

        int total      = test.getTotalQuestions() != null ? test.getTotalQuestions() : 1;
        int correct    = req.getCorrectAnswers() != null ? req.getCorrectAnswers() : 0;
        int percentage = (int) Math.round((double) correct / total * 100);
        int passing    = test.getPassingScore() != null ? test.getPassingScore() : 60;
        boolean passed = percentage >= passing;
        String feedback = generateFeedback(percentage, test.getSubject());

        TestAttempt attempt = TestAttempt.builder()
                .student(student)
                .test(test)
                .score(percentage)
                .correctAnswers(correct)
                .totalQuestions(total)
                .timeTakenSeconds(req.getTimeTakenSeconds())
                .passed(passed)
                .feedback(feedback)
                .status(TestAttempt.AttemptStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();
        return toAttemptResponse(attemptRepo.save(attempt));
    }

    /**
     * Per-student skill profile.
     * Uses JOIN FETCH query to avoid LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<TestDto.SkillProgress> getSkillProgress(Long userId) {
        User    user    = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Student student = studentRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));

        // Use the JOIN FETCH variant so test fields are loaded in-session
        List<TestAttempt> attempts = attemptRepo.findByStudentWithTest(student);

        // Best score per subject
        Map<String, Integer> best = new LinkedHashMap<>();
        for (TestAttempt a : attempts) {
            String subj = a.getTest().getSubject();
            if (subj == null) continue;
            best.merge(subj, a.getScore() != null ? a.getScore() : 0, Math::max);
        }

        return best.entrySet().stream().map(e -> {
            long count = attempts.stream()
                    .filter(a -> e.getKey().equals(a.getTest().getSubject()))
                    .count();
            return TestDto.SkillProgress.builder()
                    .subject(e.getKey())
                    .bestScore(e.getValue())
                    .level(scoreToLevel(e.getValue()))
                    .progressPercent(e.getValue())
                    .attemptsCount((int) count)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestDto.AttemptResponse> getMyAttempts(Long userId) {
        User    user    = userRepo.findById(userId).orElseThrow();
        Student student = studentRepo.findByUser(user).orElseThrow();
        return attemptRepo.findByStudentWithTest(student)
                .stream().map(this::toAttemptResponse).collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────

    private String scoreToLevel(int score) {
        if (score >= 80) return "Advanced";
        if (score >= 55) return "Intermediate";
        if (score >= 30) return "Beginner";
        return "Needs Attention";
    }

    private String generateFeedback(int score, String subject) {
        String s = subject != null ? subject : "this subject";
        if (score >= 80) return "Excellent! You have strong foundations in " + s + ". Keep pushing forward!";
        if (score >= 60) return "Good work! You understand the basics of " + s + ". Focus on the areas you missed.";
        if (score >= 40) return "You're getting there with " + s + ". Review the core concepts and try again.";
        return "This is just the beginning of your " + s + " journey. Don't be discouraged — every expert started here!";
    }

    private TestDto.TestResponse toTestResponse(Test t) {
        return TestDto.TestResponse.builder()
                .id(t.getId()).title(t.getTitle()).subject(t.getSubject())
                .category(t.getCategory()).description(t.getDescription())
                .difficulty(t.getDifficulty()).mode(t.getMode())
                .isFree(t.getIsFree()).price(t.getPrice())
                .totalQuestions(t.getTotalQuestions()).durationMinutes(t.getDurationMinutes())
                .passingScore(t.getPassingScore()).isActive(t.getIsActive())
                .displayOrder(t.getDisplayOrder()).build();
    }

    private TestDto.AttemptResponse toAttemptResponse(TestAttempt a) {
        return TestDto.AttemptResponse.builder()
                .id(a.getId())
                .testId(a.getTest().getId())
                .testTitle(a.getTest().getTitle())
                .subject(a.getTest().getSubject())
                .score(a.getScore())
                .correctAnswers(a.getCorrectAnswers())
                .totalQuestions(a.getTotalQuestions())
                .passed(a.getPassed())
                .feedback(a.getFeedback())
                .attemptedAt(a.getAttemptedAt())
                .build();
    }
}