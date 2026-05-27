package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.FounderDto;
import com.edtech.platform.entity.FounderProfile;
import com.edtech.platform.repository.FounderProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/founder")
public class FounderController {

    private final FounderProfileRepository founderRepo;

    public FounderController(FounderProfileRepository founderRepo) {
        this.founderRepo = founderRepo;
    }

    /** Public — used by the landing page */
    @GetMapping
    public ResponseEntity<ApiResponse<FounderDto.Response>> get() {
        return founderRepo.findTopByIsActiveTrueOrderByIdAsc()
                .map(f -> ResponseEntity.ok(ApiResponse.ok(toDto(f))))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }

    /** Admin — update founder profile text fields */
    @PutMapping
    public ResponseEntity<ApiResponse<FounderDto.Response>> update(
            @RequestBody FounderDto.Response req) {
        FounderProfile f = founderRepo.findTopByIsActiveTrueOrderByIdAsc()
                .orElse(FounderProfile.builder().isActive(true).build());
        if (req.getName()        != null) f.setName(req.getName());
        if (req.getTitle()       != null) f.setTitle(req.getTitle());
        if (req.getTagline()     != null) f.setTagline(req.getTagline());
        if (req.getStory()       != null) f.setStory(req.getStory());
        if (req.getLinkedinUrl() != null) f.setLinkedinUrl(req.getLinkedinUrl());
        if (req.getCity()        != null) f.setCity(req.getCity());
        return ResponseEntity.ok(ApiResponse.ok("Updated", toDto(founderRepo.save(f))));
    }

    /** Admin — upload founder photo */
    @PostMapping("/photo")
    public ResponseEntity<ApiResponse<FounderDto.Response>> uploadPhoto(
            @RequestParam("file") MultipartFile file) {
        if (file.getSize() > 5 * 1024 * 1024)
            return ResponseEntity.badRequest().body(ApiResponse.error("Max file size is 5 MB"));
        try {
            Path dir = Paths.get("uploads/founder/");
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            FounderProfile f = founderRepo.findTopByIsActiveTrueOrderByIdAsc()
                    .orElse(FounderProfile.builder().isActive(true).build());
            f.setPhotoPath("uploads/founder/" + filename);
            return ResponseEntity.ok(ApiResponse.ok("Photo uploaded", toDto(founderRepo.save(f))));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    private FounderDto.Response toDto(FounderProfile f) {
        if (f == null) return null;
        return FounderDto.Response.builder()
                .id(f.getId()).name(f.getName()).title(f.getTitle())
                .tagline(f.getTagline()).story(f.getStory())
                .photoPath(f.getPhotoPath()).linkedinUrl(f.getLinkedinUrl())
                .city(f.getCity()).build();
    }
}