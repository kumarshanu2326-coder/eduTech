package com.edtech.platform.repository;

import com.edtech.platform.entity.Student;
import com.edtech.platform.entity.Test;
import com.edtech.platform.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    /**
     * Eager-join fetch test entity to avoid LazyInitializationException
     * when accessing attempt.getTest().getSubject() outside a transaction.
     */
    @Query("SELECT ta FROM TestAttempt ta JOIN FETCH ta.test WHERE ta.student = :student ORDER BY ta.attemptedAt DESC")
    List<TestAttempt> findByStudentWithTest(@org.springframework.data.repository.query.Param("student") Student student);

    @Query("SELECT ta FROM TestAttempt ta JOIN FETCH ta.test WHERE ta.test = :test ORDER BY ta.attemptedAt DESC")
    List<TestAttempt> findByTestWithStudent(@org.springframework.data.repository.query.Param("test") Test test);

    // Keep originals for backward compat (used elsewhere)
    List<TestAttempt> findByStudentOrderByAttemptedAtDesc(Student student);
    List<TestAttempt> findByTestOrderByAttemptedAtDesc(Test test);
    Optional<TestAttempt> findTopByStudentAndTestOrderByScoreDesc(Student student, Test test);

    @Query("SELECT COUNT(DISTINCT ta.student.id) FROM TestAttempt ta")
    long countDistinctStudents();

    @Query("SELECT COUNT(ta) FROM TestAttempt ta WHERE ta.test.isFree = true")
    long countFreeTestAttempts();

    @Query("SELECT COUNT(ta) FROM TestAttempt ta WHERE ta.status = 'COMPLETED'")
    long countCompleted();

    @Query(value = """
        SELECT COUNT(DISTINCT ta.student_id) FROM test_attempts ta
        JOIN students s ON s.id = ta.student_id
        WHERE DATE(ta.attempted_at) = DATE(s.created_at)
        """, nativeQuery = true)
    long countRegisteredAndTestedSameDay();
}