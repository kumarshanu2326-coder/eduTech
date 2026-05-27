package com.edtech.platform.repository;

import com.edtech.platform.entity.StudentAnswer;
import com.edtech.platform.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByAttempt(TestAttempt attempt);
}