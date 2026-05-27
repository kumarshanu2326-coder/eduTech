package com.edtech.platform.repository;

import com.edtech.platform.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Test> findByIsFreeTrue();
    List<Test> findBySubjectIgnoreCase(String subject);
    List<Test> findByCategory(String category);
    long countByIsFreeTrue();
}
