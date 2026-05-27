package com.edtech.platform.service;

import com.edtech.platform.dto.StudentDto;
import com.edtech.platform.dto.TeacherDto;
import com.edtech.platform.entity.*;
import com.edtech.platform.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    // ── Repositories & Services ────────────────────────────────────────────────
    private final StudentRepository     studentRepo;
    private final TeacherRepository     teacherRepo;
    private final LeadRepository        leadRepo;
    private final PaymentRepository     paymentRepo;
    private final TestRepository        testRepo;
    private final TestAttemptRepository attemptRepo;
    private final SchoolRepository      schoolRepo;   // ← NEW
    private final TeacherService        teacherService;
    private final StudentService        studentService;

    public AdminService(StudentRepository studentRepo,
                        TeacherRepository teacherRepo,
                        LeadRepository leadRepo,
                        PaymentRepository paymentRepo,
                        TestRepository testRepo,
                        TestAttemptRepository attemptRepo,
                        SchoolRepository schoolRepo,             // ← NEW
                        TeacherService teacherService,
                        StudentService studentService) {
        this.studentRepo    = studentRepo;
        this.teacherRepo    = teacherRepo;
        this.leadRepo       = leadRepo;
        this.paymentRepo    = paymentRepo;
        this.testRepo       = testRepo;
        this.attemptRepo    = attemptRepo;
        this.schoolRepo     = schoolRepo;                        // ← NEW
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    // ── Dashboard stats ────────────────────────────────────────────────────────

    public DashboardStats getDashboardStats() {
        long totalStudents = studentRepo.count();
        long totalTeachers = teacherRepo.count();
        long totalAttempts = attemptRepo.count();
        long freeAttempts  = attemptRepo.countFreeTestAttempts();
        long sameDay       = attemptRepo.countRegisteredAndTestedSameDay();

        return DashboardStats.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .pendingApprovals(teacherRepo.countByApprovalStatus(Teacher.ApprovalStatus.PENDING))
                .approvedTeachers(teacherRepo.countByApprovalStatus(Teacher.ApprovalStatus.APPROVED))
                .totalLeads(leadRepo.count())
                .newLeads(leadRepo.countByStatus(Lead.LeadStatus.NEW))
                .totalRevenue(paymentRepo.totalRevenue())
                .totalTestsTaken(totalAttempts)
                .freeTestsTaken(freeAttempts)
                .studentsWhoTestedSameDay(sameDay)
                .studentsJustRegistered(totalStudents - attemptRepo.countDistinctStudents())
                .totalTests(testRepo.count())
                .totalSchools(schoolRepo.count())               // ← NEW
                .activeSchools(schoolRepo.countActive())        // ← NEW
                .schoolInquiries(schoolRepo.countInquiries())   // ← NEW
                .build();
    }

    public List<TeacherDto.Response> getTeachersByStatus(String status) {
        Teacher.ApprovalStatus approvalStatus;
        try { approvalStatus = Teacher.ApprovalStatus.valueOf(status.toUpperCase()); }
        catch (Exception e) { return teacherService.getAll(); }
        return teacherRepo.findByApprovalStatus(approvalStatus)
                .stream().map(teacherService::toResponse).collect(Collectors.toList());
    }

    // ── Admin edits student profile ────────────────────────────────────────────

    @Transactional
    public StudentDto.Response editStudentProfile(Long studentId, StudentDto.ProfileRequest req) {
        Student s = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        applyStudentFields(s, req);
        if (req.getFullName() != null) s.getUser().setFullName(req.getFullName());
        return studentService.toResponse(studentRepo.save(s));
    }

    // ── Admin edits teacher profile ────────────────────────────────────────────

    @Transactional
    public TeacherDto.Response editTeacherProfile(Long teacherId, TeacherDto.ProfileRequest req) {
        Teacher t = teacherRepo.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        applyTeacherFields(t, req);
        if (req.getFullName() != null) t.getUser().setFullName(req.getFullName());
        return teacherService.toResponse(teacherRepo.save(t));
    }

    // ── Field mappers ──────────────────────────────────────────────────────────

    private void applyStudentFields(Student s, StudentDto.ProfileRequest r) {
        if (r.getSchool()                  != null) s.setSchool(r.getSchool());
        if (r.getCurrentClass()            != null) s.setCurrentClass(r.getCurrentClass());
        if (r.getPreferredSubjects()       != null) s.setPreferredSubjects(r.getPreferredSubjects());
        if (r.getCountry()                 != null) s.setCountry(r.getCountry());
        if (r.getState()                   != null) s.setState(r.getState());
        if (r.getDistrict()                != null) s.setDistrict(r.getDistrict());
        if (r.getCity()                    != null) s.setCity(r.getCity());
        if (r.getPincode()                 != null) s.setPincode(r.getPincode());
        if (r.getFieldOfInterest()         != null) s.setFieldOfInterest(r.getFieldOfInterest());
        if (r.getWeakSubjects()            != null) s.setWeakSubjects(r.getWeakSubjects());
        if (r.getCareerAspiration()        != null) s.setCareerAspiration(r.getCareerAspiration());
        if (r.getTechInterests()           != null) s.setTechInterests(r.getTechInterests());
        if (r.getLearningHurdle()          != null) s.setLearningHurdle(r.getLearningHurdle());
        if (r.getPreferredLearningStyle()  != null) s.setPreferredLearningStyle(r.getPreferredLearningStyle());
        if (r.getCommunicationLevel()      != null) s.setCommunicationLevel(r.getCommunicationLevel());
        if (r.getInterestedInWorkshops()   != null) s.setInterestedInWorkshops(r.getInterestedInWorkshops());
        if (r.getInterestedInInternships() != null) s.setInterestedInInternships(r.getInterestedInInternships());
        if (r.getLanguagePreference()      != null) s.setLanguagePreference(r.getLanguagePreference());
    }

    private void applyTeacherFields(Teacher t, TeacherDto.ProfileRequest r) {
        if (r.getBio()             != null) t.setBio(r.getBio());
        if (r.getSubjects()        != null) t.setSubjects(r.getSubjects());
        if (r.getSubjectCategory() != null) t.setSubjectCategory(r.getSubjectCategory());
        if (r.getClassesHandled()  != null) t.setClassesHandled(r.getClassesHandled());
        if (r.getFeesPerMonth()    != null) t.setFeesPerMonth(r.getFeesPerMonth());
        if (r.getTimings()         != null) t.setTimings(r.getTimings());
        if (r.getLanguages()       != null) t.setLanguages(r.getLanguages());
        if (r.getQualifications()  != null) t.setQualifications(r.getQualifications());
        if (r.getExperienceYears() != null) t.setExperienceYears(r.getExperienceYears());
        if (r.getMeetingLink()     != null) t.setMeetingLink(r.getMeetingLink());
        if (r.getAvailability()    != null) t.setAvailability(r.getAvailability());
        if (r.getLocation()        != null) t.setLocation(r.getLocation());
        if (r.getCountry()         != null) t.setCountry(r.getCountry());
        if (r.getState()           != null) t.setState(r.getState());
        if (r.getDistrict()        != null) t.setDistrict(r.getDistrict());
        if (r.getCity()            != null) t.setCity(r.getCity());
        if (r.getArea()            != null) t.setArea(r.getArea());
        if (r.getPincode()         != null) t.setPincode(r.getPincode());
        if (r.getMode()            != null) {
            try { t.setMode(Teacher.TeachingMode.valueOf(r.getMode().toUpperCase())); }
            catch (Exception ignored) {}
        }
    }

    // ── DashboardStats DTO ─────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardStats {
        private long       totalStudents;
        private long       totalTeachers;
        private long       pendingApprovals;
        private long       approvedTeachers;
        private long       totalLeads;
        private long       newLeads;
        private BigDecimal totalRevenue;
        // Test analytics
        private long       totalTestsTaken;
        private long       freeTestsTaken;
        private long       studentsWhoTestedSameDay;
        private long       studentsJustRegistered;
        private long       totalTests;
        // School analytics (NEW)
        private long       totalSchools;
        private long       activeSchools;
        private long       schoolInquiries;
    }
}