package com.edtech.platform.repository;

import com.edtech.platform.entity.Question;
import com.edtech.platform.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestOrderByDisplayOrderAsc(Test test);
    long countByTest(Test test);
    void deleteByTest(Test test);
}