package com.edtech.platform.service;

import com.edtech.platform.dto.LeadDto;
import com.edtech.platform.entity.Lead;
import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.Teacher;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.LeadRepository;
import com.edtech.platform.repository.StudentRepository;
import com.edtech.platform.repository.TeacherRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeadService {

    private final LeadRepository    leadRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository    userRepository;
    private final NotificationService notificationService;

    public LeadService(LeadRepository leadRepository, StudentRepository studentRepository,
                       TeacherRepository teacherRepository, UserRepository userRepository,
                       NotificationService notificationService) {
        this.leadRepository      = leadRepository;
        this.studentRepository   = studentRepository;
        this.teacherRepository   = teacherRepository;
        this.userRepository      = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeadDto.Response create(Long userId, LeadDto.CreateRequest req) {
        User    user    = getUser(userId);
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        Teacher teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Lead.LeadType type = Lead.LeadType.DEMO;
        if (req.getType() != null) {
            try { type = Lead.LeadType.valueOf(req.getType().toUpperCase()); }
            catch (Exception ignored) {}
        }

        Lead lead = Lead.builder()
                .student(student)
                .teacher(teacher)
                .studentNote(req.getStudentNote())
                .type(type)
                .build();
        return toResponse(leadRepository.save(lead));
    }

    public List<LeadDto.Response> getMyLeads(Long userId) {
        User    user    = getUser(userId);
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return leadRepository.findByStudentOrderByCreatedAtDesc(student)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LeadDto.Response> getIncoming(Long userId) {
        User    user    = getUser(userId);
        Teacher teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));
        return leadRepository.findByTeacherOrderByCreatedAtDesc(teacher)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public LeadDto.Response updateStatus(Long leadId, LeadDto.StatusUpdate req) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
        Lead.LeadStatus prevStatus = lead.getStatus();
        try { lead.setStatus(Lead.LeadStatus.valueOf(req.getStatus().toUpperCase())); }
        catch (Exception ignored) {}
        if (req.getTeacherResponse() != null)
            lead.setTeacherResponse(req.getTeacherResponse());
        LeadDto.Response saved = toResponse(leadRepository.save(lead));

        // Fire email when teacher accepts a demo request
        if (Lead.LeadStatus.ACCEPTED.equals(lead.getStatus())
                && !Lead.LeadStatus.ACCEPTED.equals(prevStatus)) {
            try {
                String email      = lead.getStudent().getUser().getEmail();
                String sName      = lead.getStudent().getUser().getFullName();
                String tName      = lead.getTeacher().getUser().getFullName();
                String meetLink   = lead.getTeacher().getMeetingLink();
                String subjectStr = lead.getTeacher().getSubjects() != null
                        ? lead.getTeacher().getSubjects().split(",")[0].trim()
                        : "your subject";
                if (email != null && !email.isBlank())
                    notificationService.sendDemoApproved(email, sName, tName, subjectStr, meetLink);
            } catch (Exception ignored) { /* non-blocking */ }
        }
        return saved;
    }

    public List<LeadDto.Response> getAll() {
        return leadRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private LeadDto.Response toResponse(Lead l) {
        return LeadDto.Response.builder()
                .id(l.getId())
                .studentId(l.getStudent().getId())
                .studentName(l.getStudent().getUser().getFullName())
                .studentPhone(l.getStudent().getUser().getPhone())
                .teacherId(l.getTeacher().getId())
                .teacherName(l.getTeacher().getUser().getFullName())
                .status(l.getStatus() != null ? l.getStatus().name() : "NEW")
                .type(l.getType() != null ? l.getType().name() : "DEMO")
                .studentNote(l.getStudentNote())
                .teacherResponse(l.getTeacherResponse())
                .isPaid(l.getIsPaid())
                .demoScheduledAt(l.getDemoScheduledAt())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}