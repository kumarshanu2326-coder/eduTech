package com.edtech.platform.service;

import com.edtech.platform.dto.StudentDto;
import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.StudentRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository    userRepository;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository    = userRepository;
    }

    public StudentDto.Response getByUserId(Long userId) {
        User    user = getUser(userId);
        Student s    = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return toResponse(s);
    }

    @Transactional
    public StudentDto.Response updateProfile(Long userId, StudentDto.ProfileRequest req) {
        User    user = getUser(userId);
        Student s    = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));

        if (req.getSchool()                   != null) s.setSchool(req.getSchool());
        if (req.getCurrentClass()             != null) s.setCurrentClass(req.getCurrentClass());
        if (req.getPreferredSubjects()        != null) s.setPreferredSubjects(req.getPreferredSubjects());

        // Location
        if (req.getArea()     != null) s.setArea(req.getArea());
        if (req.getCountry()  != null) s.setCountry(req.getCountry());
        if (req.getState()    != null) s.setState(req.getState());
        if (req.getDistrict() != null) s.setDistrict(req.getDistrict());
        if (req.getCity()     != null) s.setCity(req.getCity());
        if (req.getPincode()  != null) s.setPincode(req.getPincode());

        // Deep profile
        if (req.getFieldOfInterest()          != null) s.setFieldOfInterest(req.getFieldOfInterest());
        if (req.getWeakSubjects()             != null) s.setWeakSubjects(req.getWeakSubjects());
        if (req.getCareerAspiration()         != null) s.setCareerAspiration(req.getCareerAspiration());
        if (req.getTechInterests()            != null) s.setTechInterests(req.getTechInterests());
        if (req.getLearningHurdle()           != null) s.setLearningHurdle(req.getLearningHurdle());
        if (req.getPreferredLearningStyle()   != null) s.setPreferredLearningStyle(req.getPreferredLearningStyle());
        if (req.getCommunicationLevel()       != null) s.setCommunicationLevel(req.getCommunicationLevel());
        if (req.getInterestedInWorkshops()    != null) s.setInterestedInWorkshops(req.getInterestedInWorkshops());
        if (req.getInterestedInInternships()  != null) s.setInterestedInInternships(req.getInterestedInInternships());
        if (req.getLanguagePreference()       != null) s.setLanguagePreference(req.getLanguagePreference());

        return toResponse(studentRepository.save(s));
    }

    public List<StudentDto.Response> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public StudentDto.Response getById(Long studentId) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return toResponse(s);
    }

    public StudentDto.Response toResponse(Student s) {
        return StudentDto.Response.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .fullName(s.getUser().getFullName())
                .email(s.getUser().getEmail())
                .phone(s.getUser().getPhone())
                .school(s.getSchool())
                .currentClass(s.getCurrentClass())
                .preferredSubjects(s.getPreferredSubjects())
                .area(s.getArea())
                .country(s.getCountry())
                .state(s.getState())
                .district(s.getDistrict())
                .city(s.getCity())
                .pincode(s.getPincode())
                .fieldOfInterest(s.getFieldOfInterest())
                .weakSubjects(s.getWeakSubjects())
                .careerAspiration(s.getCareerAspiration())
                .techInterests(s.getTechInterests())
                .learningHurdle(s.getLearningHurdle())
                .preferredLearningStyle(s.getPreferredLearningStyle())
                .communicationLevel(s.getCommunicationLevel())
                .interestedInWorkshops(s.getInterestedInWorkshops())
                .interestedInInternships(s.getInterestedInInternships())
                .languagePreference(s.getLanguagePreference())
                .hasPaidPlatformFee(s.getHasPaidPlatformFee())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
