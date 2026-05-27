package com.edtech.platform.repository;

import com.edtech.platform.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findByStatusOrderByCreatedAtDesc(School.SchoolStatus status);

    List<School> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(s) FROM School s WHERE s.status = 'ACTIVE' OR s.status = 'ONBOARDED'")
    long countActive();

    @Query("SELECT COUNT(s) FROM School s WHERE s.status = 'INQUIRY'")
    long countInquiries();
}