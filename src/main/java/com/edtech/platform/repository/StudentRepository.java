package com.edtech.platform.repository;

import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
}
