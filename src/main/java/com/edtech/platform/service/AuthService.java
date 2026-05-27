package com.edtech.platform.service;

import com.edtech.platform.dto.AuthDto;
import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.Teacher;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.StudentRepository;
import com.edtech.platform.repository.TeacherRepository;
import com.edtech.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository    userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NotificationService notificationService;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       TeacherRepository teacherRepository,
                       NotificationService notificationService) {
        this.userRepository      = userRepository;
        this.studentRepository   = studentRepository;
        this.teacherRepository   = teacherRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        // Validation
        if (req.getEmail() == null && req.getPhone() == null)
            throw new IllegalArgumentException("Email or phone is required");
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");
        if (req.getPhone() != null && !req.getPhone().isBlank()
                && userRepository.existsByPhone(req.getPhone()))
            throw new IllegalArgumentException("Phone already registered");

        User.Role role = User.Role.STUDENT;
        if (req.getRole() != null) {
            try { role = User.Role.valueOf(req.getRole().toUpperCase()); }
            catch (Exception ignored) {}
        }

        // NOTE: password stored plain-text until security layer is enabled
        // TODO: user.setPassword(passwordEncoder.encode(req.getPassword()));
        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail() != null && !req.getEmail().isBlank() ? req.getEmail() : null)
                .phone(req.getPhone() != null && !req.getPhone().isBlank() ? req.getPhone() : null)
                .password(req.getPassword())
                .role(role)
                .build();
        user = userRepository.save(user);

        Long profileId = null;
        if (role == User.Role.STUDENT) {
            Student s = Student.builder().user(user).build();
            profileId = studentRepository.save(s).getId();
        } else if (role == User.Role.TEACHER) {
            Teacher t = Teacher.builder().user(user).build();
            profileId = teacherRepository.save(t).getId();
        }

        log.info("Registered {} as {}", user.getEmail(), role);

        // Send welcome email (async — non-blocking)
        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                if (role == User.Role.STUDENT)
                    notificationService.sendWelcomeStudent(user.getEmail(), user.getFullName());
                else if (role == User.Role.TEACHER)
                    notificationService.sendWelcomeTeacher(user.getEmail(), user.getFullName());
            }
        } catch (Exception ignored) {}

        return buildResponse(user, profileId);
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        if (req.getIdentifier() == null || req.getIdentifier().isBlank())
            throw new IllegalArgumentException("Email or phone is required");

        User user = userRepository.findByEmail(req.getIdentifier())
                .or(() -> userRepository.findByPhone(req.getIdentifier()))
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email/phone"));

        if (!Boolean.TRUE.equals(user.getIsActive()))
            throw new IllegalArgumentException("Account is deactivated");

        // Plain text comparison (BCrypt enabled once security layer is on)
        // TODO: if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) ...
        if (!req.getPassword().equals(user.getPassword()))
            throw new IllegalArgumentException("Invalid password");

        Long profileId = null;
        if (user.getRole() == User.Role.STUDENT)
            profileId = studentRepository.findByUser(user).map(Student::getId).orElse(null);
        else if (user.getRole() == User.Role.TEACHER)
            profileId = teacherRepository.findByUser(user).map(Teacher::getId).orElse(null);

        return buildResponse(user, profileId);
    }

    private AuthDto.AuthResponse buildResponse(User u, Long profileId) {
        return AuthDto.AuthResponse.builder()
                .userId(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().name())
                .profileId(profileId)
                .token(null)   // JWT token will be set here once Spring Security is enabled
                .build();
    }
}