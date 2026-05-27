package com.edtech.platform.repository;

import com.edtech.platform.entity.Lead;
import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStudentOrderByCreatedAtDesc(Student student);
    List<Lead> findByTeacherOrderByCreatedAtDesc(Teacher teacher);
    long countByStatus(Lead.LeadStatus status);
}
