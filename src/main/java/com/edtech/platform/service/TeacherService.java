package com.edtech.platform.service;

import com.edtech.platform.dto.TeacherDto;
import com.edtech.platform.entity.Teacher;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.TeacherRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository    userRepository;
    private final NotificationService notificationService;

    // File upload base path — configure via application.properties
    private static final String UPLOAD_DIR = "uploads/teachers/";

    public TeacherService(TeacherRepository teacherRepository, UserRepository userRepository,
                          NotificationService notificationService) {
        this.teacherRepository   = teacherRepository;
        this.userRepository      = userRepository;
        this.notificationService = notificationService;
    }

    // ── Public search ──────────────────────────────────────────────────────

    public List<TeacherDto.Response> search(String subject, String classLevel, String mode,
                                            String city, String state, String district, String category) {
        return teacherRepository.search(
                blank(subject)    ? null : subject,
                blank(classLevel) ? null : classLevel,
                blank(mode)       ? null : mode.toUpperCase(),
                blank(city)       ? null : city,
                blank(state)      ? null : state,
                blank(district)   ? null : district,
                blank(category)   ? null : category
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TeacherDto.Response> getRecommended() {
        return teacherRepository
                .findByIsSchoolRecommendedTrueAndApprovalStatus(Teacher.ApprovalStatus.APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TeacherDto.Response> getFeatured() {
        return teacherRepository
                .findByIsFeaturedTrueAndApprovalStatus(Teacher.ApprovalStatus.APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TeacherDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public TeacherDto.Response getByUserId(Long userId) {
        User user = getUser(userId);
        return toResponse(teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found")));
    }

    // ── Profile update ─────────────────────────────────────────────────────

    @Transactional
    public TeacherDto.Response updateProfile(Long userId, TeacherDto.ProfileRequest req) {
        User    user = getUser(userId);
        Teacher t    = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        if (req.getBio()             != null) t.setBio(req.getBio());
        if (req.getSubjects()        != null) t.setSubjects(req.getSubjects());
        if (req.getSubjectCategory() != null) t.setSubjectCategory(req.getSubjectCategory());
        if (req.getClassesHandled()  != null) t.setClassesHandled(req.getClassesHandled());
        if (req.getFeesPerMonth()    != null) t.setFeesPerMonth(req.getFeesPerMonth());
        if (req.getTimings()         != null) t.setTimings(req.getTimings());
        if (req.getLanguages()       != null) t.setLanguages(req.getLanguages());
        if (req.getQualifications()  != null) t.setQualifications(req.getQualifications());
        if (req.getExperienceYears() != null) t.setExperienceYears(req.getExperienceYears());
        if (req.getMeetingLink()     != null) t.setMeetingLink(req.getMeetingLink());
        if (req.getAvailability()    != null) t.setAvailability(req.getAvailability());

        // Location
        if (req.getLocation() != null) t.setLocation(req.getLocation());
        if (req.getCountry()  != null) t.setCountry(req.getCountry());
        if (req.getState()    != null) t.setState(req.getState());
        if (req.getDistrict() != null) t.setDistrict(req.getDistrict());
        if (req.getCity()     != null) t.setCity(req.getCity());
        if (req.getArea()     != null) t.setArea(req.getArea());
        if (req.getPincode()  != null) t.setPincode(req.getPincode());

        if (req.getMode() != null) {
            try { t.setMode(Teacher.TeachingMode.valueOf(req.getMode().toUpperCase())); }
            catch (Exception ignored) {}
        }
        return toResponse(teacherRepository.save(t));
    }

    // ── Document upload ────────────────────────────────────────────────────

    @Transactional
    public TeacherDto.DocumentStatus uploadDocument(Long userId, String docType, MultipartFile file) {
        User    user = getUser(userId);
        Teacher t    = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        String savedPath = saveFile(file, userId);

        switch (docType.toLowerCase()) {
            case "marksheet10"   -> t.setMarksheet10Path(savedPath);
            case "marksheet12"   -> t.setMarksheet12Path(savedPath);
            case "graduation"    -> t.setGraduationMarksheetPath(savedPath);
            case "certificates"  -> t.setCertificatesPath(savedPath);
            case "experience"    -> t.setExperienceProofPath(savedPath);
            case "resume"        -> t.setResumePath(savedPath);
            default              -> throw new IllegalArgumentException("Unknown document type: " + docType);
        }
        teacherRepository.save(t);
        return buildDocStatus(t);
    }

    public TeacherDto.DocumentStatus getDocumentStatus(Long userId) {
        User    user = getUser(userId);
        Teacher t    = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));
        return buildDocStatus(t);
    }

    @Transactional
    public TeacherDto.Response uploadProfilePhoto(Long userId, MultipartFile file) {
        User    user = getUser(userId);
        Teacher t    = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));
        String savedPath = saveFile(file, userId);
        t.setProfilePhotoPath(savedPath);
        return toResponse(teacherRepository.save(t));
    }

    // ── Admin actions ──────────────────────────────────────────────────────

    public List<TeacherDto.Response> getPending() {
        return teacherRepository.findByApprovalStatus(Teacher.ApprovalStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TeacherDto.Response> getAll() {
        return teacherRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TeacherDto.Response approve(Long id) {
        Teacher t = findById(id);
        t.setApprovalStatus(Teacher.ApprovalStatus.APPROVED);
        t.setRejectionReason(null);
        TeacherDto.Response saved = toResponse(teacherRepository.save(t));
        try {
            String email = t.getUser().getEmail();
            String name  = t.getUser().getFullName();
            if (email != null && !email.isBlank())
                notificationService.sendTeacherApproved(email, name);
        } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public TeacherDto.Response reject(Long id, String reason) {
        Teacher t = findById(id);
        t.setApprovalStatus(Teacher.ApprovalStatus.REJECTED);
        t.setRejectionReason(reason);
        TeacherDto.Response saved = toResponse(teacherRepository.save(t));
        try {
            String email = t.getUser().getEmail();
            String name  = t.getUser().getFullName();
            if (email != null && !email.isBlank())
                notificationService.sendTeacherRejected(email, name, reason);
        } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public TeacherDto.Response suspend(Long id) {
        Teacher t = findById(id);
        t.setApprovalStatus(Teacher.ApprovalStatus.SUSPENDED);
        return toResponse(teacherRepository.save(t));
    }

    @Transactional
    public TeacherDto.Response toggleRecommended(Long id) {
        Teacher t = findById(id);
        t.setIsSchoolRecommended(!Boolean.TRUE.equals(t.getIsSchoolRecommended()));
        return toResponse(teacherRepository.save(t));
    }

    @Transactional
    public TeacherDto.Response toggleFeatured(Long id) {
        Teacher t = findById(id);
        t.setIsFeatured(!Boolean.TRUE.equals(t.getIsFeatured()));
        return toResponse(teacherRepository.save(t));
    }

    @Transactional
    public TeacherDto.Response activateSubscription(Long id) {
        Teacher t = findById(id);
        t.setIsSubscribed(true);
        t.setSubscriptionExpiry(LocalDateTime.now().plusMonths(1));
        return toResponse(teacherRepository.save(t));
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    public TeacherDto.Response toResponse(Teacher t) {
        boolean hasDocs = t.getMarksheet10Path() != null
                || t.getMarksheet12Path() != null
                || t.getGraduationMarksheetPath() != null;

        double rating = t.getRating() != null ? t.getRating() / 10.0 : 0.0;

        return TeacherDto.Response.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .fullName(t.getUser().getFullName())
                .email(t.getUser().getEmail())
                .phone(t.getUser().getPhone())
                .bio(t.getBio())
                .subjects(t.getSubjects())
                .subjectCategory(t.getSubjectCategory())
                .classesHandled(t.getClassesHandled())
                .feesPerMonth(t.getFeesPerMonth())
                .currency(t.getCurrency() != null ? t.getCurrency() : "INR")
                .timings(t.getTimings())
                .mode(t.getMode() != null ? t.getMode().name() : "BOTH")
                .languages(t.getLanguages())
                .qualifications(t.getQualifications())
                .experienceYears(t.getExperienceYears())
                .meetingLink(t.getMeetingLink())
                .availability(t.getAvailability())
                .location(t.getLocation())
                .country(t.getCountry())
                .state(t.getState())
                .district(t.getDistrict())
                .city(t.getCity())
                .area(t.getArea())
                .pincode(t.getPincode())
                .hasDocuments(hasDocs)
                .approvalStatus(t.getApprovalStatus() != null ? t.getApprovalStatus().name() : "PENDING")
                .rejectionReason(t.getRejectionReason())
                .isSchoolRecommended(t.getIsSchoolRecommended())
                .isFeatured(t.getIsFeatured())
                .isSubscribed(t.getIsSubscribed())
                .rating(rating)
                .reviewCount(t.getReviewCount())
                .createdAt(t.getCreatedAt())
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private TeacherDto.DocumentStatus buildDocStatus(Teacher t) {
        return TeacherDto.DocumentStatus.builder()
                .marksheet10(t.getMarksheet10Path() != null)
                .marksheet12(t.getMarksheet12Path() != null)
                .graduation(t.getGraduationMarksheetPath() != null)
                .certificates(t.getCertificatesPath() != null)
                .experienceProof(t.getExperienceProofPath() != null)
                .resume(t.getResumePath() != null)
                .approvalStatus(t.getApprovalStatus() != null ? t.getApprovalStatus().name() : "PENDING")
                .build();
    }

    private String saveFile(MultipartFile file, Long userId) {
        try {
            Path dir = Paths.get(UPLOAD_DIR + userId);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }
}