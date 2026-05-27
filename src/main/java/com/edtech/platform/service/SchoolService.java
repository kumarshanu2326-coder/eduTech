package com.edtech.platform.service;

import com.edtech.platform.dto.SchoolDto;
import com.edtech.platform.entity.School;
import com.edtech.platform.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepo;

    public SchoolService(SchoolRepository schoolRepo) {
        this.schoolRepo = schoolRepo;
    }

    // ── Public registration ───────────────────────────────────────────────────

    @Transactional
    public SchoolDto.Response register(SchoolDto.RegisterRequest req) {
        if (req.getInstitutionName() == null || req.getInstitutionName().isBlank())
            throw new IllegalArgumentException("Institution name is required");
        if (req.getContactPhone() == null && req.getContactEmail() == null)
            throw new IllegalArgumentException("Contact phone or email is required");

        School s = School.builder()
                .institutionName(req.getInstitutionName())
                .institutionType(req.getInstitutionType())
                .board(req.getBoard())
                .studentStrength(req.getStudentStrength())
                .classesFrom(req.getClassesFrom())
                .classesTo(req.getClassesTo())
                .contactName(req.getContactName())
                .contactPhone(req.getContactPhone())
                .contactEmail(req.getContactEmail())
                .designation(req.getDesignation())
                .city(req.getCity())
                .district(req.getDistrict())
                .state(req.getState())
                .pincode(req.getPincode())
                .address(req.getAddress())
                .servicesRequired(req.getServicesRequired())
                .preferredDuration(req.getPreferredDuration())
                .preferredMode(req.getPreferredMode())
                .budget(req.getBudget())
                .additionalNote(req.getAdditionalNote())
                .status(School.SchoolStatus.INQUIRY)
                .build();

        return toResponse(schoolRepo.save(s));
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SchoolDto.Response> getAll() {
        return schoolRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SchoolDto.Response> getByStatus(String status) {
        try {
            return schoolRepo.findByStatusOrderByCreatedAtDesc(
                            School.SchoolStatus.valueOf(status.toUpperCase()))
                    .stream().map(this::toResponse).collect(Collectors.toList());
        } catch (Exception e) { return getAll(); }
    }

    @Transactional(readOnly = true)
    public SchoolDto.Response getById(Long id) {
        return toResponse(schoolRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + id)));
    }

    @Transactional
    public SchoolDto.Response adminUpdate(Long id, SchoolDto.AdminUpdate req) {
        School s = schoolRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + id));
        if (req.getStatus() != null) {
            try { s.setStatus(School.SchoolStatus.valueOf(req.getStatus().toUpperCase())); }
            catch (Exception ignored) {}
        }
        if (req.getAdminNotes()      != null) s.setAdminNotes(req.getAdminNotes());
        if (req.getAssignedMentors() != null) s.setAssignedMentors(req.getAssignedMentors());
        if (req.getPackageType()     != null) s.setPackageType(req.getPackageType());
        if (req.getContractStartDate() != null) s.setContractStartDate(req.getContractStartDate());
        if (req.getContractEndDate()   != null) s.setContractEndDate(req.getContractEndDate());
        return toResponse(schoolRepo.save(s));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public SchoolDto.Response toResponse(School s) {
        return SchoolDto.Response.builder()
                .id(s.getId())
                .institutionName(s.getInstitutionName())
                .institutionType(s.getInstitutionType())
                .board(s.getBoard())
                .studentStrength(s.getStudentStrength())
                .classesFrom(s.getClassesFrom())
                .classesTo(s.getClassesTo())
                .contactName(s.getContactName())
                .contactPhone(s.getContactPhone())
                .contactEmail(s.getContactEmail())
                .designation(s.getDesignation())
                .city(s.getCity())
                .district(s.getDistrict())
                .state(s.getState())
                .address(s.getAddress())
                .servicesRequired(s.getServicesRequired())
                .preferredDuration(s.getPreferredDuration())
                .preferredMode(s.getPreferredMode())
                .budget(s.getBudget())
                .additionalNote(s.getAdditionalNote())
                .status(s.getStatus() != null ? s.getStatus().name() : "INQUIRY")
                .adminNotes(s.getAdminNotes())
                .assignedMentors(s.getAssignedMentors())
                .packageType(s.getPackageType())
                .contractStartDate(s.getContractStartDate())
                .contractEndDate(s.getContractEndDate())
                .createdAt(s.getCreatedAt())
                .build();
    }
}