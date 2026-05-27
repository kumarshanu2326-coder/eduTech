package com.edtech.platform.repository;

import com.edtech.platform.entity.FounderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FounderProfileRepository extends JpaRepository<FounderProfile, Long> {
    Optional<FounderProfile> findTopByIsActiveTrueOrderByIdAsc();
}
